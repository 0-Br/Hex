package hex;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class GUI extends JFrame
{
    Board B = new Board(); // 棋盘对象

    public GUI()
    {
        setTitle("Hex");
        setSize(Params.WIDTH, Params.HEIGHT);
        setLocationRelativeTo(null); // 窗口默认居中
        setResizable(false); // 窗口不可变更大小
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JSplitPane splitPane = new JSplitPane(); // 创建一个水平方向的分割面板
        splitPane.setDividerLocation(Params.DIV_LOCATION);
        splitPane.setDividerSize(Params.DIV_SIZE);
        splitPane.setEnabled(false); // 不可移动分割线

        JPanel mainP = new mainPanel(B);
        JPanel infoP = new infoPanel(B);
        splitPane.setLeftComponent(mainP);
        splitPane.setRightComponent(infoP);
        add(splitPane);
    }

    public static void main(String[] args)
    {
        GUI mainGUI = new GUI();
        mainGUI.setVisible(true);
    }
}

class mainPanel extends JPanel implements MouseListener
{
    boolean selected = false; // 是否被选中
    int selectedX, selectedY; // 鼠标点击的位置坐标
    Cell selected_cell; // 当前被选中的棋盘格
    Board B; // 棋盘对象

    public mainPanel(Board B)
    {
        this.B = B;

        setBackground(Params.cBACKGROUND);
        setBorder(BorderFactory.createLineBorder(Params.RED, 3));
        setLayout(null);
        for (Cell cell : B.cells)
        {
            add(cell);
            cell.setBounds(cell.loc_0, cell.loc_1, Params.A, Params.A);
        }
        addMouseListener(this);

        new Thread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            while (true)
                            {
                                try{Thread.sleep(10);}
                                catch (InterruptedException e){e.printStackTrace();}
                                while (B.time_left > 0)
                                {
                                    try{Thread.sleep(10);}
                                    catch (InterruptedException e){e.printStackTrace();}
                                }
                                settle(true);
                                B.initialize();
                            }
                        }
                    }).start();
    }

    // 结算游戏并弹窗
    public void settle(boolean TLE)
    {
        if (TLE)
        {
            B.next();
            String info = String.format("Winner is: %s\n", B.getName());
            info += String.format("Time Out!");

            UIManager.put("OptionPane.okButtonText", "Try Again!");
            JOptionPane.showMessageDialog(null, info, "Game Over!", JOptionPane.INFORMATION_MESSAGE);
            B.launch();
        }
        else
        {
            int player = B.S * -1;
            for (Cell cell : B.cells)
            {
                if (cell.state == 0)
                {
                    cell.changeTo(player);
                    if (player == 1) B.numRED++;
                    if (player == -1) B.numBLUE++;
                }
            }

            int score = B.numRED - B.numBLUE;
            String info = "Winner is: ";
            if (score > 0) info += "Red！\n";
            if (score < 0) info += "Blue！\n";
            info += String.format("Score: %d", Math.abs(score));

            UIManager.put("OptionPane.okButtonText", "Try Again!");
            JOptionPane.showMessageDialog(null, info, "Game Over!", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e)
    {
        int x = e.getX();int y = e.getY();
        int x_temp;int y_temp;

        if (B.started)
        {
            for (Cell cell : B.cells)
            {
                x_temp = x - cell.loc_0;
                y_temp = y - cell.loc_1;
                if (cell.p_out.contains(x_temp, y_temp))
                {
                    if ((!selected) && (cell.state == B.S))
                    {
                        cell.highlight(0);
                        cell.n1_highlight(1);
                        cell.n2_highlight(2);
                        selected = true;
                        selected_cell = cell;
                        return;
                    }
                    if (selected)
                    {
                        if (cell.level == 1)
                        {
                            cell.changeTo(B.S);
                            cell.infect();
                            B.release();
                        }
                        else if (cell.level == 2)
                        {
                            selected_cell.changeTo(0);
                            cell.changeTo(B.S);
                            cell.infect();
                            B.release();
                        }
                        else
                        {
                            B.release();
                            selected = false;
                            return;
                        }
                        selected = false;
                        B.count();
                        B.next();
                    }
                    // 结束检查
                    if (B.isEnd())
                    {
                        B.started = false;
                        settle(false);
                        B.initialize();
                    }
                }
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e){}
    @Override
    public void mouseReleased(MouseEvent e){}
    @Override
    public void mouseEntered(MouseEvent e){}
    @Override
    public void mouseExited(MouseEvent e){}
}

class infoPanel extends JPanel
{
    Board B;

    infoPanel(Board B)
    {
        this.B = B;

        setBackground(Params.cBOARD);
        setBorder(BorderFactory.createLineBorder(Params.BLUE, 3));
        setLayout(null);

        JButton button = new JButton("Play");
        button.setFont(new Font("Times New Roman", Font.BOLD, 18));
        button.setBounds(20, 220, 140, 60);
        button.setFocusPainted(false);
        add(button);

        button.addActionListener(new ActionListener()
                                    {
                                        public void actionPerformed(ActionEvent e)
                                        {
                                            B.initialize();
                                            button.setText("Restart");
                                        }
                                    });

        JLabel label = new JLabel(String.format("<html>Nums:<br><br>RED: %d<br><br>BLUE: %d</html>", B.numRED, B.numBLUE));
        label.setFont(new Font("Times New Roman", Font.BOLD, 16));
        label.setBounds(50, 340, 80, 120);
        label.setForeground(Params.cHIGHLIGHT_0);
        add(label);
        Timer counter = new Timer(10, new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                label.setText(String.format("<html>Nums:<br><br>RED: %d<br><br>BLUE: %d</html>", B.numRED, B.numBLUE));
            }
        });
        counter.start();

        JLabel clock = new JLabel(String.format("<html>Current Player:<br><br>%s<br><br>Time Left: %d s</html>", B.getName(), B.time_left));
        clock.setFont(new Font("Times New Roman", Font.BOLD, 16));
        clock.setBounds(30, 40, 120, 120);
        clock.setForeground(Params.cHIGHLIGHT_0);
        add(clock);
        Timer timer = new Timer(10, new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                clock.setText(String.format("<html>Current Player:<br><br>%s<br><br>Time Left: %d s</html>", B.getName(), B.time_left));
            }
        });
        timer.start();
    }
}