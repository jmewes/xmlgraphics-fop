/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */

package org.apache.fop.render.rtf;

//RTF
import org.apache.fop.render.rtf.rtflib.rtfdoc.BorderAttributesConverter;

//FOP
import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.SimpleLog;
import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.expr.NCnameProperty;
import org.apache.fop.fo.properties.ColorTypeProperty;
import org.apache.fop.fo.properties.EnumProperty;
import org.apache.fop.fo.properties.LengthProperty;
import org.apache.fop.fo.properties.ListProperty;
import org.apache.fop.fo.properties.NumberProperty;
import org.apache.fop.fo.properties.Property;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.datatypes.ColorType;

import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfAttributes;
import org.apache.fop.render.rtf.rtflib.rtfdoc.ITableAttributes;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfColorTable;

/**
 * Contributor(s):
 *  @author Roberto Marra <roberto@link-u.com>
 *  @author Boris Poudérous <boris.pouderous@eads-telecom.com>
 *  @author Normand Massé
 *  @author Peter Herweg <pherweg@web.de>
 *
 * This class was originally developed for the JFOR project and
 * is now integrated into FOP.
-----------------------------------------------------------------------------*/

/**
 * Provides methods to convert the attributes to RtfAttributes.
 */

public class TableAttributesConverter {

    private static Log log = new SimpleLog("FOP/RTF");

    //////////////////////////////////////////////////
    // @@ Construction
    //////////////////////////////////////////////////

    /**
     * Constructor.
     */
    private TableAttributesConverter() {
    }

    //////////////////////////////////////////////////
    // @@ Static converter methods
    //////////////////////////////////////////////////
    /**
     * Converts table-only attributes to rtf attributes.
     * 
     * @param attrs Given attributes
     * @param defaultAttributes Default rtf attributes
     *
     * @return All valid rtf attributes together
     *
     * @throws ConverterException On convertion error
     */
    static RtfAttributes convertTableAttributes(PropertyList propertyList)
            throws FOPException {
        RtfAttributes attrib = new RtfAttributes();

        LengthProperty lengthProp = null;
        // margin-left
        lengthProp = (LengthProperty)propertyList.get(Constants.PR_MARGIN_LEFT);
        if (lengthProp != null) {
            Float f = new Float(lengthProp.getLength().getValue() / 1000f);
            final String sValue = f.toString() + "pt";

            attrib.set(
                    ITableAttributes.ATTR_ROW_LEFT_INDENT,
                    (int)FoUnitsConverter.getInstance().convertToTwips(sValue));
        }

        return attrib;
    }

    /**
     * Converts cell attributes to rtf attributes.
     * @param attrs Given attributes
     * @param defaultAttributes Default rtf attributes
     *
     * @return All valid rtf attributes together
     *
     * @throws ConverterException On convertion error
     */
    static RtfAttributes convertCellAttributes(PropertyList props)
    throws FOPException {

        Property p;
        EnumProperty ep;
        RtfColorTable colorTable = RtfColorTable.getInstance();

        RtfAttributes attrib = null;

            attrib = new RtfAttributes();

        boolean isBorderPresent = false;

        // Cell background color
        if ((p = props.getNearestSpecified(Constants.PR_BACKGROUND_COLOR)) != null) {
            ColorType color = p.getColorType();
            if (color != null) {
                if (color.getAlpha() != 0
                        || color.getRed() != 0
                        || color.getGreen() != 0
                        || color.getBlue() != 0) {
                    attrib.set(
                        ITableAttributes.CELL_COLOR_BACKGROUND,
                        TextAttributesConverter.convertFOPColorToRTF(color));
                }
            } else {
                log.warn("Named color '" + p.toString() + "' not found. ");
            }

        }

        // Cell borders :
        if ((p = props.getExplicit(Constants.PR_BORDER_COLOR)) != null) {
            ListProperty listprop = (ListProperty) p;
            ColorType color = null;
            if (listprop.getList().get(0) instanceof NCnameProperty) {
                color = new ColorTypeProperty(((NCnameProperty)listprop.getList().get(0)).getNCname());
            } else if (listprop.getList().get(0) instanceof ColorTypeProperty) {
                color = ((ColorTypeProperty)listprop.getList().get(0)).getColorType();
            }

            attrib.set(
                BorderAttributesConverter.BORDER_COLOR,
                colorTable.getColorNumber((int)color.getRed(), (int)color.getGreen(),
                        (int)color.getBlue()).intValue());
        }
        if ((p = props.getExplicit(Constants.PR_BORDER_TOP_COLOR)) != null) {
            ColorType color = p.getColorType();
            attrib.set(
                BorderAttributesConverter.BORDER_COLOR,
                colorTable.getColorNumber((int)color.getRed(), (int)color.getGreen(),
                        (int)color.getBlue()).intValue());
        }
        if ((p = props.getExplicit(Constants.PR_BORDER_BOTTOM_COLOR)) != null) {
            ColorType color = p.getColorType();
            attrib.set(
                BorderAttributesConverter.BORDER_COLOR,
                colorTable.getColorNumber((int)color.getRed(), (int)color.getGreen(),
                        (int)color.getBlue()).intValue());
        }
        if ((p = props.getExplicit(Constants.PR_BORDER_LEFT_COLOR)) != null) {
            ColorType color = p.getColorType();
            attrib.set(
                BorderAttributesConverter.BORDER_COLOR,
                colorTable.getColorNumber((int)color.getRed(), (int)color.getGreen(),
                        (int)color.getBlue()).intValue());
        }
        if ((p = props.getExplicit(Constants.PR_BORDER_RIGHT_COLOR)) != null) {
            ColorType color = p.getColorType();
            attrib.set(
                BorderAttributesConverter.BORDER_COLOR,
                colorTable.getColorNumber((int)color.getRed(), (int)color.getGreen(),
                        (int)color.getBlue()).intValue());
        }

        // Border styles do not inherit from parent

        ep = (EnumProperty)props.get(Constants.PR_BORDER_TOP_STYLE);
        if (ep != null && ep.getEnum() != Constants.NONE) {
            attrib.set(ITableAttributes.CELL_BORDER_TOP,   "\\"
                       + convertAttributetoRtf(ep.getEnum()));
            isBorderPresent = true;
        }
        ep = (EnumProperty)props.get(Constants.PR_BORDER_BOTTOM_STYLE);
        if (ep != null && ep.getEnum() != Constants.NONE) {
            attrib.set(ITableAttributes.CELL_BORDER_BOTTOM, "\\"
                       + convertAttributetoRtf(ep.getEnum()));
            isBorderPresent = true;
        }
        ep = (EnumProperty)props.get(Constants.PR_BORDER_LEFT_STYLE);
        if (ep != null && ep.getEnum() != Constants.NONE) {
            attrib.set(ITableAttributes.CELL_BORDER_LEFT,  "\\"
                       + convertAttributetoRtf(ep.getEnum()));
            isBorderPresent = true;
        }
        ep = (EnumProperty)props.get(Constants.PR_BORDER_RIGHT_STYLE);
        if (ep != null && ep.getEnum() != Constants.NONE) {
            attrib.set(ITableAttributes.CELL_BORDER_RIGHT, "\\"
                       + convertAttributetoRtf(ep.getEnum()));
            isBorderPresent = true;
        }

        //Currently there is only one border width supported in each cell.  
        p = props.get(Constants.PR_BORDER_LEFT_WIDTH);
        if(p == null) {
            p = props.get(Constants.PR_BORDER_RIGHT_WIDTH);
        }
        if(p == null) {
            p = props.get(Constants.PR_BORDER_TOP_WIDTH);
        }
        if(p == null) {
            p = props.get(Constants.PR_BORDER_BOTTOM_WIDTH);
        }
        if (p != null) {
            LengthProperty lengthprop = (LengthProperty)p;

            Float f = new Float(lengthprop.getLength().getValue() / 1000f);
            String sValue = f.toString() + "pt";

            attrib.set(BorderAttributesConverter.BORDER_WIDTH,
                       (int)FoUnitsConverter.getInstance().convertToTwips(sValue));
        } else if (isBorderPresent) {
            //if not defined, set default border width
            //note 20 twips = 1 point
            attrib.set(BorderAttributesConverter.BORDER_WIDTH,
                       (int)FoUnitsConverter.getInstance().convertToTwips("1pt"));
        }


        // Column spanning :
        NumberProperty n = (NumberProperty)props.get(Constants.PR_NUMBER_COLUMNS_SPANNED);
        if (n != null && n.getNumber().intValue() > 1) {
            attrib.set(ITableAttributes.COLUMN_SPAN, n.getNumber().intValue());
        }

        return attrib;
    }


    /**
     * Converts table and row attributes to rtf attributes.
     *
     * @param attrs Given attributes
     * @param defaultAttributes Default rtf attributes
     *
     * @return All valid rtf attributes together
     *
     * @throws ConverterException On convertion error
     */
    static RtfAttributes convertRowAttributes(PropertyList props,
            RtfAttributes rtfatts)
    throws FOPException {

        Property p;
        EnumProperty ep;
        RtfColorTable colorTable = RtfColorTable.getInstance();

        RtfAttributes attrib = null;

            if (rtfatts == null) {
                attrib = new RtfAttributes();
            } else {
                attrib = rtfatts;
        }

        String attrValue;
        boolean isBorderPresent = false;
        //need to set a default width

        //check for keep-together row attribute
        if ((p = props.get(Constants.PR_KEEP_TOGETHER | Constants.CP_WITHIN_PAGE)) != null) {
            attrib.set(ITableAttributes.ROW_KEEP_TOGETHER);
        }

        if ((p = props.get(Constants.PR_KEEP_TOGETHER)) != null) {
            attrib.set(ITableAttributes.ROW_KEEP_TOGETHER);
        }

        //Check for keep-with-next row attribute.
        if ((p = props.get(Constants.PR_KEEP_WITH_NEXT)) != null) {
            attrib.set(ITableAttributes.ROW_KEEP_WITH_NEXT);
        }

        //Check for keep-with-previous row attribute.
        if ((p = props.get(Constants.PR_KEEP_WITH_PREVIOUS)) != null) {
            attrib.set(ITableAttributes.ROW_KEEP_WITH_PREVIOUS);
        }

        //Check for height row attribute.
        if ((p = props.get(Constants.PR_HEIGHT)) != null) {
            Float f = new Float(p.getLength().getValue() / 1000);
            attrValue = f.toString() + "pt";
            attrib.set(ITableAttributes.ROW_HEIGHT,
                       (int)FoUnitsConverter.getInstance().convertToTwips(attrValue));
        }

        /* to write a border to a side of a cell one must write the directional
         * side (ie. left, right) and the inside value if one needs to be taken
         * out ie if the cell lies on the edge of a table or not, the offending
         * value will be taken out by RtfTableRow.  This is because you can't
         * say BORDER_TOP and BORDER_HORIZONTAL if the cell lies at the top of
         * the table.  Similarly using BORDER_BOTTOM and BORDER_HORIZONTAL will
         * not work if the cell lies at th bottom of the table.  The same rules
         * apply for left right and vertical.

         * Also, the border type must be written after every control word.  Thus
         * it is implemented that the border type is the value of the border
         * place.
         */

        ep = (EnumProperty)props.get(Constants.PR_BORDER_TOP_STYLE);
        if (ep != null && ep.getEnum() != Constants.NONE) {
            attrib.set(ITableAttributes.ROW_BORDER_TOP,       "\\"
                       + convertAttributetoRtf(ep.getEnum()));
            attrib.set(ITableAttributes.ROW_BORDER_HORIZONTAL, "\\"
                       + convertAttributetoRtf(ep.getEnum()));
            isBorderPresent = true;
        }
        ep = (EnumProperty)props.get(Constants.PR_BORDER_BOTTOM_STYLE);
        if (ep != null && ep.getEnum() != Constants.NONE) {
            attrib.set(ITableAttributes.ROW_BORDER_BOTTOM,    "\\"
                       + convertAttributetoRtf(ep.getEnum()));
            attrib.set(ITableAttributes.ROW_BORDER_HORIZONTAL, "\\"
                       + convertAttributetoRtf(ep.getEnum()));
            isBorderPresent = true;
        }
        ep = (EnumProperty)props.get(Constants.PR_BORDER_LEFT_STYLE);
        if (ep != null && ep.getEnum() != Constants.NONE) {
            attrib.set(ITableAttributes.ROW_BORDER_LEFT,     "\\"
                       + convertAttributetoRtf(ep.getEnum()));
            attrib.set(ITableAttributes.ROW_BORDER_VERTICAL, "\\"
                       + convertAttributetoRtf(ep.getEnum()));
            isBorderPresent = true;
        }
        ep = (EnumProperty)props.get(Constants.PR_BORDER_RIGHT_STYLE);
        if (ep != null && ep.getEnum() != Constants.NONE) {
            attrib.set(ITableAttributes.ROW_BORDER_RIGHT,    "\\"
                       + convertAttributetoRtf(ep.getEnum()));
            attrib.set(ITableAttributes.ROW_BORDER_VERTICAL, "\\"
                       + convertAttributetoRtf(ep.getEnum()));
            isBorderPresent = true;
        }
        ep = (EnumProperty)props.get("border-horizontal-style");
        if (ep != null && ep.getEnum() != Constants.NONE) {
            attrib.set(ITableAttributes.ROW_BORDER_HORIZONTAL, "\\"
                       + convertAttributetoRtf(ep.getEnum()));
            attrib.set(ITableAttributes.ROW_BORDER_TOP,        "\\"
                       + convertAttributetoRtf(ep.getEnum()));
            attrib.set(ITableAttributes.ROW_BORDER_BOTTOM,     "\\"
                       + convertAttributetoRtf(ep.getEnum()));
            isBorderPresent = true;
        }
        ep = (EnumProperty)props.get("border-vertical-style");
        if (ep != null && ep.getEnum() != Constants.NONE) {
            attrib.set(ITableAttributes.ROW_BORDER_VERTICAL,  "\\"
                       + convertAttributetoRtf(ep.getEnum()));
            attrib.set(ITableAttributes.ROW_BORDER_LEFT,      "\\"
                       + convertAttributetoRtf(ep.getEnum()));
            attrib.set(ITableAttributes.ROW_BORDER_RIGHT,     "\\"
                       + convertAttributetoRtf(ep.getEnum()));
            isBorderPresent = true;
        }

        //Currently there is only one border width supported in each cell.  
        p = props.get(Constants.PR_BORDER_LEFT_WIDTH);
        if(p == null) {
            p = props.get(Constants.PR_BORDER_RIGHT_WIDTH);
        }
        if(p == null) {
            p = props.get(Constants.PR_BORDER_TOP_WIDTH);
        }
        if(p == null) {
            p = props.get(Constants.PR_BORDER_BOTTOM_WIDTH);
        }
        if (p != null) {
            LengthProperty lengthprop = (LengthProperty)p;

            Float f = new Float(lengthprop.getLength().getValue() / 1000f);
            String sValue = f.toString() + "pt";

            attrib.set(BorderAttributesConverter.BORDER_WIDTH,
                       (int)FoUnitsConverter.getInstance().convertToTwips(sValue));
        } else if (isBorderPresent) {
            //if not defined, set default border width
            //note 20 twips = 1 point
            attrib.set(BorderAttributesConverter.BORDER_WIDTH,
                       (int)FoUnitsConverter.getInstance().convertToTwips("1pt"));
        }

        return attrib;
    }


    /**
     *
     * @param iBorderStyle the border style to be converted
     * @return String with the converted border style
     */
    public static String convertAttributetoRtf(int iBorderStyle) {
        // Added by Normand Masse
        // "solid" is interpreted like "thin"
        if (iBorderStyle == Constants.SOLID) {
            return BorderAttributesConverter.BORDER_SINGLE_THICKNESS;
/*        } else if (iBorderStyle==Constants.THIN) {
                        return BorderAttributesConverter.BORDER_SINGLE_THICKNESS;
        } else if (iBorderStyle==Constants.THICK) {
            return BorderAttributesConverter.BORDER_DOUBLE_THICKNESS;
        } else if (iBorderStyle==Constants. value.equals("shadowed")) {
            return BorderAttributesConverter.BORDER_SHADOWED;*/
        } else if (iBorderStyle == Constants.DOUBLE) {
            return BorderAttributesConverter.BORDER_DOUBLE;
        } else if (iBorderStyle == Constants.DOTTED) {
            return BorderAttributesConverter.BORDER_DOTTED;
        } else if (iBorderStyle == Constants.DASHED) {
            return BorderAttributesConverter.BORDER_DASH;
/*        } else if (iBorderStyle==Constants value.equals("hairline")) {
            return BorderAttributesConverter.BORDER_HAIRLINE;*/
/*        } else if (iBorderStyle==Constant value.equals("dot-dash")) {
            return BorderAttributesConverter.BORDER_DOT_DASH;
        } else if (iBorderStyle==Constant value.equals("dot-dot-dash")) {
            return BorderAttributesConverter.BORDER_DOT_DOT_DASH;
        } else if (iBorderStyle==Constant value.equals("triple")) {
            return BorderAttributesConverter.BORDER_TRIPLE;
        } else if (iBorderStyle==Constant value.equals("wavy")) {
            return BorderAttributesConverter.BORDER_WAVY;
        } else if (iBorderStyle==Constant value.equals("wavy-double")) {
            return BorderAttributesConverter.BORDER_WAVY_DOUBLE;
        } else if (iBorderStyle==Constant value.equals("striped")) {
            return BorderAttributesConverter.BORDER_STRIPED;
        } else if (iBorderStyle==Constant value.equals("emboss")) {
            return BorderAttributesConverter.BORDER_EMBOSS;
        } else if (iBorderStyle==Constant value.equals("engrave")) {
            return BorderAttributesConverter.BORDER_ENGRAVE;*/
        } else {
            return null;
        }
    }

}
