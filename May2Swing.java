package UDP_May1;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class May2Swing extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	public static final int pieces = 1024 * 32;
	File selectedDirectory;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					May2Swing frame = new May2Swing();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 * @throws SocketException 
	 * @throws UnknownHostException 
	 */
	public May2Swing() throws SocketException, UnknownHostException {
		DatagramSocket clientSocket = new DatagramSocket(2002);
        InetAddress serverAddress = InetAddress.getByName("localhost");
		setTitle("MÁY 2");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 735, 437);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("Gửi file:");
		lblNewLabel.setBounds(31, 31, 370, 28);
		lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 14));
		contentPane.add(lblNewLabel);
		
		JLabel lblStatus = new JLabel("Status: Đang chờ...");
		lblStatus.setBounds(31, 107, 300, 28);
		lblStatus.setFont(new Font("Tahoma", Font.PLAIN, 14));
		contentPane.add(lblStatus);
		
		JButton btnNewButton = new JButton("Gửi");
		btnNewButton.setBounds(432, 32, 191, 27);
		btnNewButton.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnNewButton.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		    	new Thread(() -> {
		            JFileChooser fileChooser = new JFileChooser();
		            int returnValue = fileChooser.showOpenDialog(null);
		            if (returnValue == JFileChooser.APPROVE_OPTION) {
		                File selectedFile = fileChooser.getSelectedFile();
		                String sourcePath = selectedFile.getAbsolutePath() + File.separator ;
		                String destinationDir = "";
		                lblNewLabel.setText("Gửi file: " + selectedFile.getName()); 
		                
		                try {
		                    DatagramPacket sendPacket;

		                    File fileSend = new File(sourcePath);
		                    InputStream inputStream = new FileInputStream(fileSend);
		                    BufferedInputStream bis = new BufferedInputStream(inputStream);

		                    byte[] bytePart = new byte[pieces];

		                    // get file size
		                    long fileLength = fileSend.length();
		                    int piecesOfFile = (int) (fileLength / pieces);
		                    int lastByteLength = (int) (fileLength % pieces);

		                    // check last bytes of file
		                    if (lastByteLength > 0) {
		                        piecesOfFile++;
		                    }

		                    // split file into pieces and assign to fileBytess
		                    byte[][] fileBytess = new byte[piecesOfFile][pieces];
		                    int count = 0;
		                    while (bis.read(bytePart, 0, pieces) > 0) {
		                        fileBytess[count++] = bytePart;
		                        bytePart = new byte[pieces];
		                    }

		                    // read file info
		                    FileInfo fileInfo = new FileInfo();
		                    fileInfo.setFilename(fileSend.getName());
		                    fileInfo.setFileSize(fileSend.length());
		                    fileInfo.setPiecesOfFile(piecesOfFile);
		                    fileInfo.setLastByteLength(lastByteLength);
		                    fileInfo.setDestinationDirectory(destinationDir);

		                    // send file info
		                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
		                    ObjectOutputStream oos = new ObjectOutputStream(baos);
		                    oos.writeObject(fileInfo);
		                    sendPacket = new DatagramPacket(baos.toByteArray(), baos.toByteArray().length,
		                            serverAddress, 2003);
		                    clientSocket.send(sendPacket);
		                    lblStatus.setText("Đang gửi file..."); 
		                    

		                    // send pieces of file
		                    for (int i = 0; i < (count - 1); i++) {
		                        sendPacket = new DatagramPacket(fileBytess[i], pieces,
		                                serverAddress, 2003);
		                        clientSocket.send(sendPacket);
		                        Thread.sleep(40);
		                    }

		                    // send last bytes of file
		                    sendPacket = new DatagramPacket(fileBytess[count - 1], pieces,
		                            serverAddress, 2003);
		                    clientSocket.send(sendPacket);
		                    Thread.sleep(40);

		                    bis.close();
		                    
		                    // Update status after sending file
		                    lblStatus.setText("Gửi file thành công!"); 

		                } catch (UnknownHostException e1) {
		                    e1.printStackTrace();
		                    lblStatus.setText("Lỗi: Không thể kết nối tới server");
		                } catch (IOException e1) {
		                    e1.printStackTrace();
		                    lblStatus.setText("Lỗi: Không thể gửi file");
		                } catch (InterruptedException e1) {
		                    e1.printStackTrace();
		                    lblStatus.setText("Lỗi: Quá trình gửi file bị gián đoạn");
		                }
		            }
		        }).start();
		    }
		});
		contentPane.add(btnNewButton);
		
		Canvas canvas = new Canvas();
		canvas.setBounds(31, 188, 644, 1);
		canvas.setBackground(new Color(0, 0, 0));
		contentPane.add(canvas);
		
		JLabel lblNhnFile = new JLabel("Nhận file: ");
		lblNhnFile.setBounds(31, 225, 370, 28);
		lblNhnFile.setFont(new Font("Tahoma", Font.PLAIN, 14));
		contentPane.add(lblNhnFile);
		
		JLabel lblWaitingForResponse = new JLabel("Status: Waiting for response...");
		lblWaitingForResponse.setBounds(31, 291, 275, 28);
		lblWaitingForResponse.setFont(new Font("Tahoma", Font.PLAIN, 14));
		contentPane.add(lblWaitingForResponse);
		
		JLabel lblVTrLu = new JLabel("Vị trí lưu: ");
		lblVTrLu.setBounds(31, 266, 275, 28);
		lblVTrLu.setFont(new Font("Tahoma", Font.PLAIN, 14));
		contentPane.add(lblVTrLu);
		
		DatagramSocket serverSocket = new DatagramSocket(2000);
    	lblWaitingForResponse.setText("Server is opened on " + 2000);
		JButton btnNewButton_1 = new JButton("Chọn thư mục nhận");
		btnNewButton_1.setBounds(432, 225, 191, 28);
		btnNewButton_1.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnNewButton_1.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		    	new Thread(new Runnable() {
					@Override
					public void run() {
						JFileChooser directoryChooser = new JFileChooser();
		                directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		                int returnValue = directoryChooser.showOpenDialog(null);
		                if (returnValue == JFileChooser.APPROVE_OPTION) {
		                    selectedDirectory = directoryChooser.getSelectedFile();
		                    System.out.println("Directory selected: " + selectedDirectory.getAbsolutePath());
		                    lblVTrLu.setText("Vị trí lưu: " + selectedDirectory.getAbsolutePath());
		                }
		                while (true) {
			                try {
			                	byte[] receiveData = new byte[pieces];
				                DatagramPacket receivePacket;
			                    // get file info
			                    receivePacket = new DatagramPacket(receiveData, receiveData.length);
			                    serverSocket.receive(receivePacket);
			                    InetAddress inetAddress = receivePacket.getAddress();
			                    ByteArrayInputStream bais = new ByteArrayInputStream(receivePacket.getData());
			                    ObjectInputStream ois = new ObjectInputStream(bais);
			                    FileInfo fileInfo = (FileInfo) ois.readObject();
			                    String destinationDir = selectedDirectory.getAbsolutePath() + File.separator;
			                	fileInfo.setDestinationDirectory(destinationDir);
			                	
			                    // show file info
			                    if (fileInfo != null) {
			                    	lblNhnFile.setText("Nhận file: " + fileInfo.getFilename());
			                        System.out.println("File name: " + fileInfo.getFilename());
			                        System.out.println("File size: " + fileInfo.getFileSize());
			                        System.out.println("Pieces of file: " + fileInfo.getPiecesOfFile());
			                        System.out.println("Last bytes length: " + fileInfo.getLastByteLength());
			                    }
			                    // get file content
			                    lblWaitingForResponse.setText("Receiving file...");
			                    File fileReceive = new File(fileInfo.getDestinationDirectory() 
			                            + fileInfo.getFilename());
			                    BufferedOutputStream bos = new BufferedOutputStream(
			                            new FileOutputStream(fileReceive));
			                    // write pieces of file
			                    for (int i = 0; i < (fileInfo.getPiecesOfFile() - 1); i++) {
			                        receivePacket = new DatagramPacket(receiveData, receiveData.length, 
			                                inetAddress, 2001);
			                        serverSocket.receive(receivePacket);
			                        bos.write(receiveData, 0, pieces);
			                    }
			                    // write last bytes of file
			                    receivePacket = new DatagramPacket(receiveData, receiveData.length, 
			                            inetAddress, 2001);
			                    serverSocket.receive(receivePacket);
			                    bos.write(receiveData, 0, fileInfo.getLastByteLength());
			                    bos.flush();
			                    lblWaitingForResponse.setText("Done!");

			                    // close stream
			                    bos.close();
			                } catch (IOException e) {
			                    e.printStackTrace();
			                } catch (ClassNotFoundException e) {
			                    e.printStackTrace();
			                }
							
						} 					
					}
						
				}).start();
		    }
		});
		contentPane.add(btnNewButton_1);
	}

}
