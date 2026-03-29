package hex;

import java.awt.*;
import javax.swing.*;
import java.util.*;

class Cell extends JPanel
{
    int pos_0, pos_1; // 序列坐标
    int loc_0, loc_1; // 位置坐标
    int state = 0; // 状态
    int level = -1; // 高亮级别

    // 图形框架
    Polygon p_out, p_in;

    HashSet<Cell> n1_cells = new HashSet<Cell>(); // 1阶邻居集合
    HashSet<Cell> n2_cells = new HashSet<Cell>(); // 2阶邻居集合

    Cell()
    {
        super();
        setOpaque(false); // 背景透明

        p_out = new Polygon();
        p_in = new Polygon();
        for (int i = 0; i < 6; i++)
        {
            p_out.addPoint((int)(Params.A / 2 + Params.R_out * Math.cos((2 * i + 1) * Math.PI / 6)),
                (int)(Params.A / 2 + Params.R_out * Math.sin((2 * i + 1) * Math.PI / 6)));
            p_in.addPoint((int)(Params.A / 2 + Params.R_in * Math.cos((2 * i + 1) * Math.PI / 6)),
                (int)(Params.A / 2 + Params.R_in * Math.sin((2 * i + 1) * Math.PI / 6)));
        }
    }

    // 设置坐标
    void setPos(int pos_0, int pos_1)
    {
        this.pos_0 = pos_0; this.pos_1 = pos_1;
        loc_0 = Params.D * (pos_0 - 4); loc_1 = Params.D_v * (pos_1 - 2);
    }
    // 计算到另一个棋盘格的距离，采用等级制
    int calDistanceTo(Cell cell_p)
    {
        int distance = (pos_0 - cell_p.pos_0) * (pos_0 - cell_p.pos_0)
                        + (pos_1 - cell_p.pos_1) * (pos_1 - cell_p.pos_1) * 4;
        if ((0 < distance) && (distance <= 1)) return 0;
        if ((1 < distance) && (distance <= 5)) return 1;
        if ((5 < distance) && (distance <= 20)) return 2;
        else return 3;
    }

    // 更改棋盘格状态
    void changeTo(int state){this.state = state; repaint();}
    // 将邻接的对方棋子的状态修改为己方
    void infect()
    {
        for (Cell cell_p : n1_cells)
        {
            if (cell_p.state == state * -1) cell_p.changeTo(this.state);
        }
    }

    // 显示高亮
    void highlight(int level){this.level = level; repaint();}
    // 激发可供移动的1阶邻居
    void n1_highlight(int level)
    {
        for (Cell cell_p : n1_cells)
        {
            if (cell_p.state == 0) cell_p.highlight(level);
        }
    }
    // 激发可供移动的2阶邻居
    void n2_highlight(int level)
    {
        for (Cell cell_p : n2_cells)
        {
            if (cell_p.state == 0) cell_p.highlight(level);
        }
    }
    // 检查1阶邻居中是否存在某类的棋子
    boolean n1_check(int state)
    {
        for (Cell cell_p : n1_cells)
        {
            if (cell_p.state == state) return true;
        }
        return false;
    }
    // 检查2阶邻居中是否存在某类的棋子
    boolean n2_check(int state)
    {
        for (Cell cell_p : n2_cells)
        {
            if (cell_p.state == state) return true;
        }
        return false;
    }

    @Override
    // 图像绘制函数
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        g.setColor(Params.cBOARD);
        g.fillPolygon(p_out);

        if (state == 1) g.setColor(Params.RED);
        if (state == -1) g.setColor(Params.BLUE);
        g.fillPolygon(p_in);

        if (level == 0) g.setColor(Params.cHIGHLIGHT_0);
        if (level == 1) g.setColor(Params.cHIGHLIGHT_1);
        if (level == 2) g.setColor(Params.cHIGHLIGHT_2);
        g.drawPolygon(p_in);
    }
}

class Board
{
    Cell board[][] = new Cell[26][13]; // 9x9棋盘
    HashSet<Cell> cells = new HashSet<Cell>(); // 全体有效的棋盘格

    boolean started = false; // 游戏是否开始
    int S; // 当前行棋玩家：1为红，-1为蓝
    int numRED, numBLUE; // 当前棋盘上的棋子数量

    Thread t; // 计时线程
    int time_left = Params.TIME_LIMIT; // 剩余时间

    Board()
    {
        for (int i = 0; i < 26; i++)
        {
            for (int j = 0; j < 13; j++)
            {
                if ((3 < i) && (i < 22) && (1 < j) && (j < 11))
                {
                    if (((i % 2 == 0) && (j % 2 == 0)) || ((i % 2 != 0) && (j % 2 != 0)))
                    {
                        board[i][j] = new Cell();
                        board[i][j].setPos(i, j);
                        cells.add(board[i][j]);
                    }
                }
            }
        }
        for (Cell cell : cells)
        {
            for (Cell cell_p : cells)
            {
                if (cell.calDistanceTo(cell_p) == 1) cell.n1_cells.add(cell_p);
                if (cell.calDistanceTo(cell_p) == 2) cell.n2_cells.add(cell_p);
            }
        }
        launch();
    }

    // 启动计时线程
    void launch()
    {
        t = new Thread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                while (time_left > 0)
                                {
                                    try{Thread.sleep(999);}
                                    catch (InterruptedException e){e.printStackTrace();}
                                    if (started) time_left--;
                                }
                            }
                        });
        t.start();
    }

    // 初始化，并开始游戏
    void initialize()
    {
        started = true;
        S = 1; // 红方先行
        time_left = Params.TIME_LIMIT;

        for (Cell cell : cells)
        {
            cell.changeTo(0);
            cell.highlight(-1);
        }

        // 初始放置4颗棋子
        board[7][3].changeTo(-1); board[17][9].changeTo(-1);
        board[7][9].changeTo(1); board[17][3].changeTo(1);
        count();
    }

    // 切换行棋状态
    void next(){S *= -1; time_left = Params.TIME_LIMIT;}

    // 棋盘解除高亮
    void release(){for (Cell cell : cells) cell.highlight(-1);}

    // 统计棋盘上的红蓝棋子数量
    void count()
    {
        numRED = 0;
        numBLUE = 0;
        for (Cell cell : cells)
        {
            if (cell.state == 1) numRED++;
            if (cell.state == -1) numBLUE++;
        }
    }

    // 获得当前行棋方的颜色名
    String getName()
    {
        if (S == 1) return "Red";
        if (S == -1) return "Blue";
        else return "Not Started Yet";
    }

    // 游戏结束判定
    // 检查当前玩家是否还可以继续行棋，若不能，返回true
    boolean isEnd()
    {
        for (Cell cell : cells)
        {
            if (cell.state == S)
            {
                if (cell.n1_check(0)) return false;
                if (cell.n2_check(0)) return false;
            }
        }
        return true;
    }
}