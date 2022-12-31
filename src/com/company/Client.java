package com.company;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.net.SocketException;

/**
 * The type Client.
 */
public class Client {
    /**
     * The constant scanner.
     */
    public static Scanner sc = new Scanner(System.in);
    private static PrintWriter cout;
    /**
     * The constant name.
     */
    public static String name,
    /**
     * The File name.
     */
    fileName;
    /**
     * The constant writer.
     */
    public static BufferedWriter writer = null;
    /**
     * The constant txtFile.
     */
    public static File txtFile;


    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        name = "";
        String message = "";
        System.out.println("enter port num :");
        int port = sc.nextInt();
        try (Socket socket = new Socket("localhost", port)) {
            cout = new PrintWriter(new OutputStreamWriter(
                    socket.getOutputStream(), StandardCharsets.UTF_8), true);
            ThreadClient threadClient = new ThreadClient(socket);
            Thread cli = new Thread(threadClient);
            name = threadClient.register(cout);
            cli.start();
            System.out.print("\t* Leave command : \u001B[36m\"exit\"\u001B[0m\n" +
                    "\t* Chat history showing command : \u001B[36m\"history\"\u001B[0m\n" +
                    "\t* Whenever you was ready, type \u001B[36m\"ready\"\u001B[0m\n\nready?  ");
            while (true) {
                if (!sc.nextLine().equals("ready"))
                    continue;
                cout.println('\u200e' + name + " is ready");
                break;
            }
            saveChats();
            while (true) {
                message = sc.nextLine();
                if (message.equals("history"))
                    showHistory();
                else if (message.equals("exit")) {
                    cout.println(message);
                    throw new SocketException();
                } else
                    cout.println(message);
            }
        } catch (Exception e) {
            try {
                if (writer != null)
                    writer.close();
                if (cout != null)
                    cout.close();
                if (txtFile!=null)
                txtFile.delete();
            } catch (IOException ex) {
                ex.printStackTrace(System.err);
            }
        }
    }

    /**
     * Save.
     *
     * @param message the message to send
     */
    public static void save(String message) {
        try {
            writer.write(message + "\n");
            writer.flush();
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    /**
     * opens a file to save chats.
     */
    public static void saveChats() {
        txtFile = new File(fileName);
        try {
            writer = new BufferedWriter(new FileWriter(txtFile));

        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
    }

    /**
     * Shows chat history.
     */
    public static void showHistory() {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(txtFile));
            int count;
            char[] buffer = new char[2048];
            System.out.println("\t\t** Chat History **\n");
            while (reader.ready()) {
                count = reader.read(buffer);
                System.out.println(new String(buffer, 0, count));
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace(System.err);
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        } finally {
            try {
                if (reader != null)
                    reader.close();
            } catch (IOException ex) {
                ex.printStackTrace(System.err);
            }
        }
        System.out.println("\nAll history is shown above...");
    }
}

/**
 * The type Thread client.
 */
class ThreadClient implements Runnable {

    private Socket socket;
    private BufferedReader in;
    private InputStream io;
    private Player role;
    private String name;

    /**
     * Instantiates a new Thread client.
     *
     * @param socket the socket
     * @throws IOException the io exception
     */
    public ThreadClient(Socket socket) throws IOException {
        this.socket = socket;
        io = socket.getInputStream();
        this.in = new BufferedReader(new InputStreamReader(io, "UTF-8"));
    }

    @Override
    public void run() {
        try {
            while (true) {
                String message = in.readLine();
                if (message.equals("role"))
                    setRole(in.readLine());
                else if (message.equals("act"))
                    role.showAct();
                else {
                    System.out.println(message);
                    Client.save(message);
                }
            }
        } catch (Exception e) {
            System.out.println("\t\t" + '\u200e' + "\u001B[33m" + "You left the chat \u001B[0m");
            try {
                in.close();
                System.exit(0);
            } catch (IOException io) {
                io.printStackTrace();
            }
        }
    }

    /**
     * Registers a name.
     *
     * @param cout the printwriter
     * @return the name
     */
    public String register(PrintWriter cout) {
        Scanner sc = new Scanner(System.in);
        try {
            while (true) {
                System.out.println("Enter your name : ");
                String s = sc.nextLine();
                name = "\"" + s + "\"";
                cout.println(name);
                if (in.readLine().equals("Welcome...")) {
                    Client.fileName = s + ".txt";
                    break;
                } else
                    System.out.println("\t> Name already exists; Try again. <");
            }
        } catch (IOException io) {
            System.out.println(io);
        }
        return name;
    }

    /**
     * creates a player subclass instance.
     *
     * @param ch the ch
     */
    public void setRole(String ch) {
        role = Character.valueOf(ch).getPlayer();
        role.setName(name);
        System.out.println("\u001B[40m\t* Your Character Is : " + "\u001B[31m" + Character.valueOf(ch) + "    \u001B[0m");
    }
}