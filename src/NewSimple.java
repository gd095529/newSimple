import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class NewSimple {
    public static Connection makeDB() {

        Connection con = null;
        Statement stmt = null;
        String url = "jdbc:mysql://localhost:3306/micom";
        String id = "root";
        String password = "root";
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");// 유연한 클래스부르기
            System.out.println("드라이버 적재 성공");
            con = DriverManager.getConnection(url, id, password);
            System.out.println("데이터베이스 연결 성공");
        } catch (ClassNotFoundException e) {
            System.out.println("드라이버를 찾을 수 없습니다.");
        } catch (SQLException sqle) {
            System.out.println("연결에 실패하였습니다.");
        }
        return con;
    }

    private JFrame frame;
    private JTextField addArea;
    private JButton resultB;
    private JButton resetB;
    private JButton debugB;
    private JScrollPane scrollPane;
    private JTextArea textArea;
    private JFrame debug;
    private JFrame result;
    private JTextField tinstructionCounter;
    private JTextField tinstructionRegister;
    private JTextField toperationCode;
    private JTextField toperand;
    private JTextField taccumulators;

    // 100개의 메모리 슬롯과 누산기 정의
    int instructionCounter = 0;
    int current_instruction = 0;
    int accumulator = 0;
    int memory[] = new int[100];
    int instructionRegister = 0;
    int operationCode = 0;
    int operand = 0;
    String log = "";
    int lnum = 0;

    // 모든 옵코드 정의
    final int READ = 10;
    final int WRITE = 11;
    final int LOAD = 20;
    final int STORE = 21;
    final int ADD = 30;
    final int SUBTRACT = 31;
    final int MULTIPLY = 33;
    final int DIVIDE = 32;
    final int BRANCH = 40;
    final int BRANCHNEG = 41;
    final int BRANCHZERO = 42;
    final int HALT = 43;



    /**
     * Create the application.
     */
    public NewSimple() {
        initialize();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        Connection conn = makeDB();

        frame = new JFrame();
        frame.getContentPane().setBackground(new Color(138, 43, 226));
        frame.setBounds(100, 100, 800, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);

        scrollPane = new JScrollPane();
        scrollPane.setBounds(12, 10, 760, 288);
        frame.getContentPane().add(scrollPane);

        textArea = new JTextArea();
        textArea.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        textArea.setEditable(false);
        textArea.setText("*** 심플트론에 오신 것을 환영합니다 ***\n" + "*** 하나의 명령을 한 번에 추가 버튼을 클릭하여 입력하십시오. ***\n"
                + "*** 완료 버튼을 클릭한 후 실행하여 프로그램을 실행할 수 있습니다. ***\n" + "*** 프로그램을 진행하려면 다음 버튼을 클릭할 수 있습니다. ***\n\n");
        scrollPane.setViewportView(textArea);

        addArea = new JTextField();
        addArea.setBounds(12, 308, 279, 29);
        addArea.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        addArea.setForeground(new Color(0, 0, 0));
        frame.getContentPane().add(addArea);
        addArea.setColumns(10);

        JButton addB = new JButton("입력");
//      addB.addKeyListener(new KeyAdapter() {
//         @Override
//         public void keyPressed(KeyEvent e) {
//            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
//               log = addArea.getText();
//               textArea.append(log);
//               log = "";
//               addArea.setText("");
//            }
//         }
//      });

        addB.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                log = addArea.getText();
                try {
                    lnum = Integer.parseInt(log);
                    if(lnum == -99999) textArea.append("\r\n***프로그램 로딩 완료***\r\n***시작하겠습니다.***\r\n");
                    else if(lnum< -9999 || lnum> +9999) {
                        //초기화 작업 메서드 넣어주기

                        throw new OutOfMemoryError();
                    }

                    if(lnum>=0) {
//                  log = "%02d ? +%04d", operand,log
                    }
                    textArea.append(log);
                    log = "";
                    addArea.setText("");
                }catch(Exception ex) {
                    textArea.append("\r\n정확한 값을 입력해주세요\r\n");
                }catch(OutOfMemoryError om) {
                    textArea.append("\r\n***범위를 벗어났기 때문에 메모리를 초기화 하겠습니다.***\r\n");
                }
            }
        });


        addB.setBounds(303, 308, 97, 29);
        addB.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        addB.setForeground(new Color(0, 0, 0));
        frame.getContentPane().add(addB);

        resultB = new JButton("결과");
        resultB.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                result = new JFrame();
                result.getContentPane().setBackground(new Color(255, 204, 255));
                result.setBounds(100, 100, 670, 646);
                result.getContentPane().setLayout(null);
                result.setVisible(true);

                taccumulators = new JTextField();
                taccumulators.setBounds(12, 35, 116, 21);
                result.getContentPane().add(taccumulators);
                taccumulators.setColumns(10);
                taccumulators.setEditable(false);

                tinstructionCounter = new JTextField();
                tinstructionCounter.setColumns(10);
                tinstructionCounter.setBounds(140, 35, 116, 21);
                result.getContentPane().add(tinstructionCounter);
                tinstructionCounter.setEditable(false);

                tinstructionRegister = new JTextField();
                tinstructionRegister.setColumns(10);
                tinstructionRegister.setBounds(268, 35, 116, 21);
                result.getContentPane().add(tinstructionRegister);
                tinstructionRegister.setEditable(false);

                toperationCode = new JTextField();
                toperationCode.setColumns(10);
                toperationCode.setBounds(396, 35, 116, 21);
                result.getContentPane().add(toperationCode);
                toperationCode.setEditable(false);

                toperand = new JTextField();
                toperand.setColumns(10);
                toperand.setBounds(524, 35, 116, 21);
                result.getContentPane().add(toperand);
                toperand.setEditable(false);

                JLabel lblNewLabel = new JLabel("accumulator");
                lblNewLabel.setBounds(32, 21, 88, 15);
                result.getContentPane().add(lblNewLabel);

                JLabel lblInstructioncounter = new JLabel("instructionCounter");
                lblInstructioncounter.setBounds(140, 21, 116, 15);
                result.getContentPane().add(lblInstructioncounter);

                JLabel lblInstructionregister = new JLabel("instructionRegister");
                lblInstructionregister.setBounds(268, 21, 130, 15);
                result.getContentPane().add(lblInstructionregister);

                JLabel lblOperationcode = new JLabel("operationCode");
                lblOperationcode.setBounds(411, 21, 88, 15);
                result.getContentPane().add(lblOperationcode);

                JLabel lblOperand = new JLabel("operand");
                lblOperand.setBounds(552, 21, 88, 15);
                result.getContentPane().add(lblOperand);

                JButton btnNewButton = new JButton("<-");
                btnNewButton.setFont(new Font("맑은 고딕", Font.BOLD, 30));
                btnNewButton.setBounds(31, 498, 97, 67);
                result.getContentPane().add(btnNewButton);

                JButton btnNewButton_1 = new JButton("->");
                btnNewButton_1.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                    }
                });
                btnNewButton_1.setFont(new Font("맑은 고딕", Font.BOLD, 30));
                btnNewButton.setBounds(31, 498, 97, 67);
                result.getContentPane().add(btnNewButton);

                btnNewButton_1.setBounds(140, 498, 97, 67);
                result.getContentPane().add(btnNewButton_1);

                JScrollPane scrollPane = new JScrollPane();
                scrollPane.setBounds(22, 66, 606, 411);
                result.getContentPane().add(scrollPane);

                JTextArea dumArea = new JTextArea();
                scrollPane.setViewportView(dumArea);
            }
        });
        resultB.setBounds(675, 308, 97, 29);
        resultB.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        resultB.setForeground(new Color(0, 0, 0));
        frame.getContentPane().add(resultB);

        resetB = new JButton("초기화");
        resetB.setBounds(566, 308, 97, 29);
        resetB.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        resetB.setForeground(new Color(0, 0, 0));
        frame.getContentPane().add(resetB);

        debugB = new JButton("디버그");
        debugB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            }
        });
        debugB.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                debug = new JFrame();
                debug.getContentPane().setBackground(new Color(138, 43, 226));
                debug.setBounds(100, 100, 800, 600);
                debug.getContentPane().setLayout(null);
                debug.setVisible(true);
            }
        });
        debugB.setBounds(457, 308, 97, 29);
        debugB.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        debugB.setForeground(new Color(0, 0, 0));
        frame.getContentPane().add(debugB);

    }
    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    NewSimple window = new NewSimple();
                    window.frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}