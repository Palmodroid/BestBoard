package org.lattilad.bestboard.parser;

import java.util.Arrays;

/**
 * Simplified class to store color mnemonics
 * Because these data are final, no sort method is used in run-time.
 * Data should be sorted previously!
 */
public class ColorToken
    {
    // Transparency is always 0xFF
    private static final int[] VALUE =
            {
            0x0066ff, 0x0099ff, 0xf0f8ff, 0xfaebd7, 0x00ffff, 0x7fffd4, 0xf0ffff, 0x0000ff,
            0x0033ff, 0x3300ff, 0xf5f5dc, 0xffe4c4, 0x000000, 0xffebcd, 0x0000ff, 0x8a2be2,
            0xa52a2a, 0xdeb887, 0x00ffff, 0x5f9ea0, 0x00ccff, 0x00ffcc, 0x7fff00, 0xd2691e,
            0xff7f50, 0x6495ed, 0xfff8dc, 0xdc143c, 0x00ffff, 0x003399, 0x006699, 0x00008b,
            0x008b8b, 0xb8860b, 0xa9a9a9, 0x006400, 0xa9a9a9, 0xbdb76b, 0x8b008b, 0x556b2f,
            0xff8c00, 0x9932cc, 0x8b0000, 0xe9967a, 0x8fbc8f, 0x483d8b, 0x2f4f4f, 0x2f4f4f,
            0x00ced1, 0x9400d3, 0x0033cc, 0x3300cc, 0x0099cc, 0x00cc99, 0x336699, 0x333399,
            0x339999, 0x339933, 0x993399, 0x996633, 0x993366, 0x993333, 0x669933, 0x339966,
            0x663399, 0x999933, 0xff1493, 0x00bfff, 0x000099, 0x009999, 0x009900, 0x990099,
            0x990000, 0x999900, 0x666666, 0x33cc00, 0x00cc33, 0x0066cc, 0x0000cc, 0x00cccc,
            0x00cc00, 0xcc00cc, 0xcc6600, 0xcc0066, 0xcc0000, 0x66cc00, 0x00cc66, 0x6600cc,
            0xcccc00, 0x696969, 0x696969, 0xcc0099, 0x9900cc, 0x1e90ff, 0x993300, 0x996600,
            0x990066, 0x990033, 0xcc3300, 0xcc0033, 0x339900, 0x669900, 0x009966, 0x009933,
            0x330099, 0x660099, 0x333366, 0x336666, 0x336633, 0x663366, 0x663333, 0x666633,
            0xcc9900, 0x99cc00, 0xb22222, 0xfffaf0, 0x228b22, 0xff00ff, 0x00ff00, 0xdcdcdc,
            0x33ff00, 0x00ff33, 0xf8f8ff, 0xffd700, 0xdaa520, 0x808080, 0x008000, 0xadff2f,
            0x808080, 0xf0fff0, 0xff69b4, 0xcd5c5c, 0x4b0082, 0xfffff0, 0x000000, 0xf0e68c,
            0x6699ff, 0x66ccff, 0xe6e6fa, 0xfff0f5, 0x7cfc00, 0x3366ff, 0x6633ff, 0x33ccff,
            0x33ffcc, 0x6699cc, 0x6666cc, 0x66cccc, 0x66cc66, 0xcc66cc, 0xcc9966, 0xcc6699,
            0xcc6666, 0x99cc66, 0x66cc99, 0x9966cc, 0xcccc66, 0xfffacd, 0x6666ff, 0x66ffff,
            0x66ff66, 0xff66ff, 0xff6666, 0xffff66, 0x999999, 0x66ff33, 0x33ff66, 0x3399ff,
            0x3333ff, 0x33ffff, 0x33ff33, 0xff33ff, 0xff9933, 0xff3399, 0xff3333, 0x99ff33,
            0x33ff99, 0x9933ff, 0xffff33, 0xadd8e6, 0xf08080, 0xe0ffff, 0xfafad2, 0xd3d3d3,
            0x90ee90, 0xd3d3d3, 0xffb6c1, 0xffa07a, 0x20b2aa, 0x87cefa, 0x778899, 0x778899,
            0xb0c4de, 0xffffe0, 0x00ff00, 0x32cd32, 0xfaf0e6, 0xff33cc, 0xcc33ff, 0xff9966,
            0xffcc66, 0xff66cc, 0xff6699, 0xff6633, 0xff3366, 0x99ff66, 0xccff66, 0x66ffcc,
            0x66ff99, 0x9966ff, 0xcc66ff, 0x9999cc, 0x99cccc, 0x99cc99, 0xcc99cc, 0xcc9999,
            0xcccc99, 0xffcc33, 0xccff33, 0xff00ff, 0x3366cc, 0x3399cc, 0xff00ff, 0x800000,
            0x66cdaa, 0x0000cd, 0xba55d3, 0x9370db, 0x3cb371, 0x7b68ee, 0x00fa9a, 0x48d1cc,
            0xc71585, 0x3333cc, 0x33cccc, 0x33cc33, 0xcc33cc, 0xcc3333, 0xcccc33, 0x191970,
            0xf5fffa, 0xffe4e1, 0xff00cc, 0xcc00ff, 0xffe4b5, 0xcc6633, 0xcc9933, 0xcc3399,
            0xcc3366, 0x66cc33, 0x99cc33, 0x33cc99, 0x33cc66, 0x6633cc, 0x9933cc, 0x666699,
            0x669999, 0x669966, 0x996699, 0x996666, 0x999966, 0xffdead, 0x000080, 0x003366,
            0x000066, 0x006666, 0x006600, 0x660066, 0x663300, 0x660033, 0x660000, 0x336600,
            0x006633, 0x330066, 0x666600, 0x333333, 0xfdf5e6, 0x808000, 0x6b8e23, 0xff6600,
            0xff9900, 0xffa500, 0xff4500, 0xda70d6, 0x000033, 0x003333, 0x003300, 0x330033,
            0x330000, 0x333300, 0xeee8aa, 0x98fb98, 0xafeeee, 0xdb7093, 0xffefd5, 0x99ccff,
            0x9999ff, 0x99ffff, 0x99ff99, 0xff99ff, 0xffcc99, 0xff99cc, 0xff9999, 0xccff99,
            0x99ffcc, 0xcc99ff, 0xffff99, 0xffdab9, 0xcd853f, 0xcccccc, 0xffc0cb, 0xdda0dd,
            0xb0e0e6, 0xff0099, 0xff0066, 0x800080, 0xccccff, 0xccffff, 0xccffcc, 0xffccff,
            0xffcccc, 0xffffcc, 0xff0000, 0x663399, 0xff0000, 0xbc8f8f, 0x4169e1, 0xff3300,
            0xff0033, 0x8b4513, 0xfa8072, 0xf4a460, 0x2e8b57, 0xfff5ee, 0xa0522d, 0xc0c0c0,
            0x87ceeb, 0x6a5acd, 0x708090, 0x708090, 0xfffafa, 0x00ff7f, 0x66ff00, 0x99ff00,
            0x4682b4, 0xd2b48c, 0x008080, 0xd8bfd8, 0xff6347, 0x00ff99, 0x00ff66, 0x40e0d0,
            0xee82ee, 0x6600ff, 0x9900ff, 0xffffff, 0xf5deb3, 0xffffff, 0xf5f5f5, 0xffff00,
            0xffff00, 0x9acd32, 0xffcc00, 0xccff00
            };

    // The two arrays should be complementary!
    // Strings should be in alphabetical order!
    private static final String[] TOKEN =
            {
            "aab", "aac", "aliceblue", "antiquewhite", "aqua", "aquamarine", "azure", "b",
            "bba", "bbv", "beige", "bisque", "black", "blanchedalmond", "blue", "blueviolet",
            "brown", "burlywood", "c", "cadetblue", "cca", "cct", "chartreuse", "chocolate",
            "coral", "cornflowerblue", "cornsilk", "crimson", "cyan", "dab", "dac", "darkblue",
            "darkcyan", "darkgoldenrod", "darkgray", "darkgreen", "darkgrey", "darkkhaki",
            "darkmagenta", "darkolivegreen", "darkorange", "darkorchid", "darkred", "darksalmon",
            "darkseagreen", "darkslateblue", "darkslategray", "darkslategrey", "darkturquoise",
            "darkviolet", "dba", "dbv", "dca", "dct", "dda", "ddb", "ddc", "ddg", "ddm", "ddo",
            "ddp", "ddr", "dds", "ddt", "ddv", "ddy", "deeppink", "deepskyblue", "dfb", "dfc",
            "dfg", "dfm", "dfr", "dfy", "dg", "dgs", "dgt", "dha", "dhb", "dhc", "dhg", "dhm",
            "dho", "dhp", "dhr", "dhs", "dht", "dhv", "dhy", "dimgray", "dimgrey", "dmp", "dmv",
            "dodgerblue", "dor", "doy", "dpm", "dpr", "dro", "drp", "dsg", "dsy", "dtc", "dtg",
            "dvb", "dvm", "dwb", "dwc", "dwg", "dwm", "dwr", "dwy", "dyo", "dys", "firebrick",
            "floralwhite", "forestgreen", "fuchsia", "g", "gainsboro", "ggs", "ggt", "ghostwhite",
            "gold", "goldenrod", "gray", "green", "greenyellow", "grey", "honeydew", "hotpink",
            "indianred", "indigo", "ivory", "k", "khaki", "lab", "lac", "lavender",
            "lavenderblush", "lawngreen", "lba", "lbv", "lca", "lct", "lda", "ldb", "ldc", "ldg",
            "ldm", "ldo", "ldp", "ldr", "lds", "ldt", "ldv", "ldy", "lemonchiffon", "lfb", "lfc",
            "lfg", "lfm", "lfr", "lfy", "lg", "lgs", "lgt", "lha", "lhb", "lhc", "lhg", "lhm",
            "lho", "lhp", "lhr", "lhs", "lht", "lhv", "lhy", "lightblue", "lightcoral",
            "lightcyan", "lightgoldenrodyellow", "lightgray", "lightgreen", "lightgrey",
            "lightpink", "lightsalmon", "lightseagreen", "lightskyblue", "lightslategray",
            "lightslategrey", "lightsteelblue", "lightyellow", "lime", "limegreen", "linen",
            "lmp", "lmv", "lor", "loy", "lpm", "lpr", "lro", "lrp", "lsg", "lsy", "ltc", "ltg",
            "lvb", "lvm", "lwb", "lwc", "lwg", "lwm", "lwr", "lwy", "lyo", "lys", "m", "mab",
            "mac", "magenta", "maroon", "mediumaquamarine", "mediumblue", "mediumorchid",
            "mediumpurple", "mediumseagreen", "mediumslateblue", "mediumspringgreen",
            "mediumturquoise", "mediumvioletred", "mfb", "mfc", "mfg", "mfm", "mfr", "mfy",
            "midnightblue", "mintcream", "mistyrose", "mmp", "mmv", "moccasin", "mor", "moy",
            "mpm", "mpr", "msg", "msy", "mtc", "mtg", "mvb", "mvm", "mwb", "mwc", "mwg", "mwm",
            "mwr", "mwy", "navajowhite", "navy", "oda", "odb", "odc", "odg", "odm", "odo", "odp",
            "odr", "ods", "odt", "odv", "ody", "og", "oldlace", "olive", "olivedrab", "oor", "ooy",
            "orange", "orangered", "orchid", "owb", "owc", "owg", "owm", "owr", "owy",
            "palegoldenrod", "palegreen", "paleturquoise", "palevioletred", "papayawhip",
            "pda", "pdb", "pdc", "pdg", "pdm", "pdo", "pdp", "pdr", "pds", "pdt", "pdv", "pdy",
            "peachpuff", "peru", "pg", "pink", "plum", "powderblue", "ppm", "ppr", "purple", "pwb",
            "pwc", "pwg", "pwm", "pwr", "pwy", "r", "rebeccapurple", "red", "rosybrown",
            "royalblue", "rro", "rrp", "saddlebrown", "salmon", "sandybrown", "seagreen",
            "seashell", "sienna", "silver", "skyblue", "slateblue", "slategray", "slategrey",
            "snow", "springgreen", "ssg", "ssy", "steelblue", "tan", "teal", "thistle",
            "tomato", "ttc", "ttg", "turquoise", "violet", "vvb", "vvm", "w", "wheat",
            "white", "whitesmoke", "y", "yellow", "yellowgreen", "yyo", "yys"
            };

    /**
     * Finds color value for string token. Return value is -1L if token is not found.
     * @param token string representative of the color
     * @return color value
     */
    public static long get( String token )
        {
        int n = Arrays.binarySearch( TOKEN, token );
        return n < 0 ? -1L : 0xFF000000L | (long)VALUE[n];
        }
    }
