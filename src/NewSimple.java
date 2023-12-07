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

import javax.swing.*;

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
    private JOptionPane JOptionpane;
    private static final int MEMORYSIZE = 100;

    // 100개의 메모리 슬롯과 누산기 정의
    int instructionCounter = 0;
    int current_instruction = 0;
    int accumulator = 0;
    int memory[] = new int[MEMORYSIZE];
    int instructionRegister = 0;
    int operationCode = 0;
    int operand = 0;
    String log = "";
    int lnum = 0;

    // 모든 옵코드 정의
    private static final int READ = 10;
    private static final int WRITE = 11;
    private static final int LOAD = 20;
    private static final int STORE = 21;
    private static final int ADD = 30;
    private static final int SUBSTRACT = 31;
    private static final int MULTIPLY = 33;
    private static final int DIVIDE = 32;
    private static final int BRANCH = 40;
    private static final int BRANCHNEG = 41;
    private static final int BRANCHZERO = 42;
    private static final int HALT = 43;

    /**
     * Create the application.
     */
    public NewSimple() {
        initialize();
    }

    //메모리 화면 출력용
    public static void printRegistersAndMemory(int accumulator,int instructionCounter
            ,int instructionRegister, int operationCode, int operand, int[] memory) {
        //출력

        System.out.println("REGISTERS :");
        if(accumulator>=0)
            System.out.printf("%-25s"+"+%04d\r\n","accumulator",accumulator);
        else
            System.out.printf("%-25s"+"%05d\r\n","accumulator",accumulator);

        System.out.printf("%-28s"+"%02d\r\n","instructionCounter",instructionCounter);
        System.out.printf("%-25s"+"+%4d\r\n","instructionRegister",instructionRegister);
        System.out.printf("%-28s"+"%02d\r\n","operationCode",operationCode);
        System.out.printf("%-28s"+"%02d\r\n","operand",operand);
        System.out.println();
        System.out.println("MEMORY :");
        System.out.printf("%5s"," ");
        for(int i =0; i<10; i++) {
            System.out.printf("%-3s%5d"," ",i);
        }

        for(int i =0,j =0; i<MEMORYSIZE; i++,j++) {
            if(i%10==0)
                System.out.printf("\r\n%5d",j);

            if(memory[i]>=0)
                System.out.printf("%-3s+%04d"," ",memory[i]);
            else
                System.out.printf("%-3s%05d"," ",memory[i]);
        }
        System.out.println("\r\n\r\n");
        //출력
    }


    //메모리 읽어버리는 메서드
    public void readMemory(){
        while(true) { //??전체적인 break, return, continue 다시생각...
            instructionRegister = memory[instructionCounter]; //현재 실행문장
            if(instructionRegister<0)
                textArea.append("*** Data position Error ***");

            operationCode = instructionRegister/100;//앞 2개( 명령어)
            operand = instructionRegister%100;

            switch(operationCode) {//명령어 확인

                case READ :

                    textArea.append("\r\n*** insert value, -9999<= value <= 9999 ***\r\n");
                    memory[operand] = Integer.parseInt(JOptionPane.showInputDialog("정수를 입력하세요:"));
                    //memory[operand]=scan.nextInt();
                    if((memory[operand]<-9999||memory[operand]>+9999)) {
                        textArea.append("\r\n*** because out of range, Simpletron execution terminated ***\r\n");
                        //리턴말고 모든 버튼 비활, 리셋만 활성화 해야하나??
                        return;
                    }
                    break;

                case WRITE :
                    //write 어떻게 표현할지 함더 생각 ??
                    textArea.append("\r\nWRITE : "+memory[operand]+"\r\n");
                    break;

                case LOAD :
                    accumulator = memory[operand];//데이터를
                    break;

                case STORE :
                    memory[operand] = accumulator; //누산기값을
                    break;

                case ADD :
                    accumulator += memory[operand];//데이터를
                    break;

                case SUBSTRACT :
                    accumulator -= memory[operand];//데이터를
                    break;

                case DIVIDE :
                    if(memory[operand]==0) {//데이터가
                        textArea.append("\r\n*** Attempt to divide by zero ***\r\n");
                        return;
                    }
                    accumulator /= memory[operand];//데이터를
                    break;

                case MULTIPLY :
                    accumulator *= memory[operand];//데이터를
                    break;

                case BRANCH :
                    instructionCounter = operand;//주소값으로 이동
                    continue;

                case BRANCHNEG :
                    if(accumulator<0) {
                        instructionCounter = operand;//주소값으로 이동
                        continue;
                    }
                    break;

                case BRANCHZERO :
                    if(accumulator == 0) {
                        instructionCounter = operand;//다음 실행문장 주소값으로 이동
                        continue;
                    }
                    break;

                case HALT :
                    //?? 여기는 메모리 보여줘야함
//                    printRegistersAndMemory(accumulator,instructionCounter,instructionRegister
//                            , operationCode, operand, memory);
                    textArea.append("\r\n*** Simpletron execution terminated ***\r\n");
                    return;

                default :
                    textArea.append("\r\n*** Doesn't exist operationCode ***\r\n");

                    return;
            }

            //??메모리 보여주는거 다른작업끝난뒤
//            printRegistersAndMemory(accumulator,instructionCounter,instructionRegister
//                    , operationCode, operand, memory);

            //만약 branch 일경우 continue없애고 이거용 확인 변수생성, if문으로 아닌경우에만 넣기??
            instructionCounter++;//다음 메모리 위치
        }
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
                    if(lnum == -99999) {
                        textArea.append("\r\n***프로그램 로딩 완료***\r\n***시작하겠습니다.***\r\n");
                        //메모리 다 읽어버리기??


                        //입력 버튼 비활코드??

                        //리셋버튼으로 가서 클릭하면 입력 활성화??
                    }
                    else if(lnum< -9999 || lnum> +9999) {
                        //초기화 작업 메서드 넣어주기??

                        throw new OutOfMemoryError();
                    }

                    if(lnum>=0) {
                        log = String.format("%02d ? +%04d\r\n", operand, lnum);
                    }else{
                        log = String.format("%02d ? %05d\r\n", operand, lnum);
                    }
                    textArea.append(log);
                    memory[operand] =lnum;
                    operand++;
                    //값 다 넣었으면 항상 초기화??더있는지 확인
                    log = "";
                    lnum = 0;
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