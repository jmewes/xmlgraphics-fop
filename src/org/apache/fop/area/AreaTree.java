/*
 * $Id$
 * Copyright (C) 2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.area;

import org.apache.fop.render.Renderer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Area tree for formatting objects.
 *
 * Concepts:
 * The area tree is to be as small as possible. With minimal classes
 * and data to fully represent an area tree for formatting objects.
 * The area tree needs to be simple to render and follow the spec
 * closely.
 * This area tree has the concept of page sequences.
 * Where ever possible information is discarded or optimised to
 * keep memory use low. The data is also organised to make it
 * possible for renderers to minimise their output.
 * A page can be saved if not fully resolved and once rendered
 * a page contains only size and id reference information.
 * The area tree pages are organised in a model that depends on the
 * type of renderer.
 */
public class AreaTree {
    // allows for different models to deal with adding/rendering
    // in different situations
    private AreaTreeModel model;

    // hashmap of arraylists containing pages with id area
    private Map idLocations = new HashMap();
    // list of id's yet to be resolved and arraylists of pages
    private Map resolve = new HashMap();
    private List treeExtensions = new ArrayList();

    /**
     * Create a render pages area tree model.
     * @param rend the renderer that will be used
     * @return RenderPagesModel the new area tree model
     */
    public static RenderPagesModel createRenderPagesModel(Renderer rend) {
        return new RenderPagesModel(rend);
    }

    /**
     * Create a new store pages model.
     * @return StorePagesModel the new model
     */
    public static StorePagesModel createStorePagesModel() {
        return new StorePagesModel();
    }

    /**
     * Set the tree model to use for this area tree.
     * The different models can have different behaviour
     * when pages area added and other changes.
     * @param m the area tree model
     */
    public void setTreeModel(AreaTreeModel m) {
        model = m;
    }

    /**
     * Start a new page sequence.
     * This signals that a new page sequence has started in the document.
     * @param title the title of the new page sequence or null if no title
     */
    public void startPageSequence(Title title) {
        model.startPageSequence(title);
    }

    /**
     * Add a new page to the area tree.
     * @param page the page to add
     */
    public void addPage(PageViewport page) {
        model.addPage(page);
    }

    /**
     * Add an id reference pointing to a page viewport.
     * @param id the id of the reference
     * @param pv the page viewport that contains the id reference
     */
    public void addIDRef(String id, PageViewport pv) {
        List list = (List)idLocations.get(id);
        if (list == null) {
            list = new ArrayList();
            idLocations.put(id, list);
        }
        list.add(pv);

        Set todo = (Set)resolve.get(id);
        if (todo != null) {
            for (Iterator iter = todo.iterator(); iter.hasNext();) {
                Resolveable res = (Resolveable)iter.next();
                res.resolve(id, list);
            }
            resolve.remove(id);
        }
    }

    /**
     * Get the list of id references for an id.
     * @param id the id to lookup
     * @return the list of id references.
     */
    public List getIDReferences(String id) {
        return (List)idLocations.get(id);
    }

    /**
     * Add an unresolved object with a given id.
     * @param id the id reference that needs resolving
     * @param res the Resolveable object to resolve
     */
    public void addUnresolvedID(String id, Resolveable res) {
        Set todo = (Set)resolve.get(id);
        if (todo == null) {
            todo = new HashSet();
            resolve.put(id, todo);
        }
        todo.add(res);
    }

    /**
     * Add a tree extension.
     * This checks if the extension is resolveable and attempts
     * to resolve or add the resolveable ids for later resolution.
     * @param ext the tree extension to add.
     */
    public void addTreeExtension(TreeExt ext) {
        treeExtensions.add(ext);
        if (ext.isResolveable()) {
            Resolveable res = (Resolveable)ext;
            String[] ids = res.getIDs();
            for (int count = 0; count < ids.length; count++) {
                if (idLocations.containsKey(ids[count])) {
                    res.resolve(ids[count], (List)idLocations.get(ids[count]));
                } else {
                    Set todo = (Set)resolve.get(ids[count]);
                    if (todo == null) {
                        todo = new HashSet();
                        resolve.put(ids[count], todo);
                    }
                    todo.add(ext);
                }
            }
        } else {
            handleTreeExtension(ext, TreeExt.IMMEDIATELY);
        }
    }

    /**
     * Handle a tree extension.
     * This sends the extension to the model for handling.
     * @param ext the tree extension to handle
     * @param when when the extension should be handled by the model
     */
    public void handleTreeExtension(TreeExt ext, int when) {
        // queue tree extension according to the when
        model.addExtension(ext, when);
    }

    /**
     * Signal end of document.
     * This indicates that the document is complete and any unresolved
     * reference can be dealt with.
     */
    public void endDocument() {
        for (Iterator iter = resolve.keySet().iterator(); iter.hasNext();) {
            String id = (String)iter.next();
            Set list = (Set)resolve.get(id);
            for (Iterator resIter = list.iterator(); resIter.hasNext();) {
                Resolveable res = (Resolveable)resIter.next();
                if (!res.isResolved()) {
                    res.resolve(id, null);
                }
            }
        }
        model.endDocument();
    }
}

