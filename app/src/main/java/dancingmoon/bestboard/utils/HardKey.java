package dancingmoon.bestboard.utils;

import android.view.KeyEvent;

public class HardKey
    {
    private static String[] title = {
            "N/A", "SFL", "SFR", "HOME", "BACK", "CALL", "END",
            "·0·", "·1·", "·2·", "·3·", "·4·", "·5·", "·6·", "·7·", "·8·", "·9·", "·*·", "·#·",
            "UP", "DN", "LEFT", "RIGHT", "CENTER", "VOL+", "VOL-",
            "POWER", "CAM", "CLEAR", "·A·", "·B·", "·C·", "·D·", "·E·", "·F·", "·G·",
            "·H·", "·I·", "·J·", "·K·", "·L·", "·M·", "·N·", "·O·", "·P·", "·Q·", "·R·", "·S·", "·T·",
            "·U·", "·V·", "·W·", "·X·", "·Y·", "·Z·", "·,·", "·.·",
            "LALT", "RALT", "LSFT", "RSFT", "TAB", "SPACE",
            "SYM", "EXPL", "ENV", "ENTER", "DEL",
            "·`·", "·-·", "·=·", "·[·", "·]·", "·\\·", "·;·", "·\'·", "·/·", "·@·",
            "NUM", "HOOK", "FOCUS", "·+·", "MENU", "NOTIF", "SRCH",
            "P/P", "STOP", "NEXT", "PREV", "REW", "FFW", "MUTE",
            "PGUP", "PGDN", "PSYM", "SWCHR",
            "·BA·", "·BB·", "·BC·", "·BX·", "·BY·", "·BZ·", "·L1·", "·R1·", "·L2·", "·R2·",
            "LTH", "RTH", "STA", "SEL", "MOD", "ESC", "FWDEL",
            "LCTR", "RCTR", "CPSLK", "SCRLK", "LMETA", "RMETA", "FUNCT",
            "SYSRQ", "BREAK", "MVHOM", "MVEND", "INSERT", "FWD", "PLAY", "PAUSE",
            "CLOSE", "EJECT", "REC", "·F1·", "·F2·", "·F3·", "·F4·", "·F5·", "·F6·",
            "·F7·", "·F8·", "·F9·", "·F10·", "·F11·", "·F12·", "NUMLK",
            "·N0·", "·N1·", "·N2·", "·N3·", "·N4·", "·N5·", "·N6·", "·N7·", "·N8·", "·N9·",
            "·N/·", "·N*·", "·N-·", "·N+·", "·N.·", "·N,·", "·NCR·", "·N=·", "·N(·", "·N)·",
            "VOLM", "INFO", "CH+", "CH-", "ZOOM+", "ZOOM-", "TV",
            "WIN", "GUIDE", "DVR", "BKMRK", "CAPT", "SET", "TVPW", "TVIN",
            "STPPW", "TPIN", "AVRPW", "AVRIN", "RED", "GREEN", "YELL", "BLUE",
            "APPSW", "·B1·", "·B2·", "·B3·", "·B4·", "·B5·", "·B6·", "·B7·", "·B8·", "·B9·",
            "·B10·", "·B11·", "·B12·", "·B13·", "·B14·", "·B15·", "·B16·",
            "LANG", "MANNER", "3D", "CONT", "CALD", "MUSIC", "CALC",
            "ZHK", "EI", "MU", "HEN", "KSW", "YEN", "RO", "KANA", "AST",
            "BR-", "BR+", "AUTRK", "SLEEP", "WAKE", "PAIR", "TMENU",
            "·11·", "·12·", "LSTCH", "TVD", "VAS", "TVR", "TXT", "TVN", "TTA", "TTD",
            "SAT", "SBS", "SCS", "SRV", "NET", "ANT", "HDM1", "HDM2", "HDM3", "HDM4",
            "CPS1", "CPS2", "CMP1", "CMP2", "VGA", "AUD", "AU+", "AU-",
            "ZMD", "CNM", "CTX", "TIM", "HELP" };


    public static String getString(int code)
        {
        if ( code >= 0 && code < title.length )
            return title[code];
        return "";
        }


    public static char convertFromAscii( char ascii )
        {

        if ( ascii >= '0' && ascii <= '9' )
            return (char) (ascii - '0' + KeyEvent.KEYCODE_0);

        if ( ascii >= 'a' && ascii <= 'z' )
            return (char) (ascii - 'a' + KeyEvent.KEYCODE_A);

        if ( ascii >= 'A' && ascii <= 'Z' )
            return (char) (ascii - 'A' + KeyEvent.KEYCODE_A);

        switch( ascii )
            {
            case '*': return KeyEvent.KEYCODE_STAR;
            case '#': return KeyEvent.KEYCODE_POUND;
            case ',': return KeyEvent.KEYCODE_COMMA;
            case '.': return KeyEvent.KEYCODE_PERIOD;
            case '\t': return KeyEvent.KEYCODE_TAB;
            case ' ': return KeyEvent.KEYCODE_SPACE;
            case '\n': return KeyEvent.KEYCODE_ENTER;
            case '`': return KeyEvent.KEYCODE_GRAVE;
            case '-': return KeyEvent.KEYCODE_MINUS;
            case '=': return KeyEvent.KEYCODE_EQUALS;
            case '[': return KeyEvent.KEYCODE_LEFT_BRACKET;
            case ']': return KeyEvent.KEYCODE_RIGHT_BRACKET;
            case '\\': return KeyEvent.KEYCODE_BACKSLASH;
            case ';': return KeyEvent.KEYCODE_SEMICOLON;
            case '\'': return KeyEvent.KEYCODE_APOSTROPHE;
            case '/': return KeyEvent.KEYCODE_SLASH;
            case '@': return KeyEvent.KEYCODE_AT;
            case '+': return KeyEvent.KEYCODE_PLUS;

            default: return KeyEvent.KEYCODE_UNKNOWN;
            }
        }
    }
