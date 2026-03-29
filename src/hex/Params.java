package hex;

import java.awt.*;

class Params
{
    final static int WIDTH = 800; // Frame的宽度
    final static int HEIGHT = 560; // Frame的高度
    final static int DIV_LOCATION = 610; // 横向分割线的位置
    final static int DIV_SIZE = 2; // 横向分割线的宽度
    final static int A = 100; // 棋盘格Panel的边长
    final static int D = 30; // 棋盘格半间距
    final static int D_v = (int)(D * Math.sqrt(3)); // 棋盘格垂直间距
    final static int R_out = 32; // 棋盘格六边形的半径
    final static int R_in = 30; // 棋子六边形的半径
    final static int TIME_LIMIT = 30; // 时间限制

    // 颜色参数
    final static Color RED = new Color(236, 65, 67);
    final static Color BLUE = new Color(39, 136, 181);
    final static Color cBACKGROUND = new Color(54, 60, 67);
    final static Color cBOARD = new Color(80, 89, 97);
    final static Color cHIGHLIGHT_0 = new Color(252, 252, 253);
    final static Color cHIGHLIGHT_1 = new Color(89, 161, 58);
    final static Color cHIGHLIGHT_2 = new Color(215, 148, 59);
}