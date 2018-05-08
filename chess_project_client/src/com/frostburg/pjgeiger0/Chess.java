package com.frostburg.pjgeiger0;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.event.ActionEvent;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;


public class Chess extends Application {

    Scene scene;
    Scene sceneGame;

    public static boolean turn = true;                 //whos turn it is true =white
    private volatile boolean side = true; //determines what the user can control: true is white, false is black
    private volatile String selectedUser = "";

    public static int   movement = 0;
    public static final int TILE_SIZE = 100;
    public static final int WIDTH = 8;
    public static final int HEIGHT = 8;

    private Tile[][] board = new Tile[WIDTH][HEIGHT];

    private Group tileGroup = new Group();
    private Group pieceGroup = new Group();
    private ObservableList<String> items;
    private Label invites;



    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        InetAddress ip = InetAddress.getByName("localhost");
        int port = 4000;
        Client client = new Client(port, ip);
        client.start();

        sceneGame = new Scene(createContent());


        primaryStage.setTitle("Sign In");
        primaryStage.setMinHeight(600);
        primaryStage.setMinHeight(800);

        Label label1 = new Label("Type your username below.");
        invites = new Label("");
        TextField textField = new TextField();
        Button signInButton = new Button("Sign_in");
        ListView<String> list = new ListView<String>();
        items = FXCollections.observableArrayList("1");
        list.setItems(items);
        Button yesButton = new Button("Yes");
        Button noButton = new Button("No");
        Button refreshButton = new Button("Refresh");
        Button sendInvite = new Button("Invite");
        //set the no button, yes button, and refresh to disabled until the user signs in
        noButton.setDisable(true);
        yesButton.setDisable(true);
        refreshButton.setDisable(true);
        sendInvite.setDisable(true);

        //yes button starts the scene for now, but this will change later possibly
        yesButton.setOnAction(e -> primaryStage.setScene(sceneGame));

        //this will send the client's username to the server and then disable the button
        //also enables the rest of the buttons
        signInButton.setOnAction(e -> Platform.runLater(new Runnable() {
            @Override
            public void run() {
                client.sendUserNameToServer(textField.getText());
                refreshButton.setDisable(false);
                yesButton.setDisable(false);
                noButton.setDisable(false);
                signInButton.setDisable(true);
                sendInvite.setDisable(false);
                client.sendMessageToServer("--list");
            }
        }));
        
        refreshButton.setOnAction(e -> client.sendMessageToServer("--list"));

        //user clicks on a name in the list, and the selected user to send an invite to changes with it
        list.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                selectedUser = newValue;
            }
        });

        sendInvite.setOnAction(e -> client.sendMessageToServer("--join " + selectedUser));

        HBox layout = new HBox();
        layout.getChildren().addAll(label1, textField, signInButton, list, yesButton, noButton, refreshButton, sendInvite, invites);
        layout.setSpacing(10);
        Scene scene = new Scene(layout);


        StackPane layoutOther = new StackPane();
        primaryStage.setTitle("Chess");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    class Client extends Thread{
        private Socket client;
        private DataOutputStream output;
        private IncomingData incoming;

        public Client(int port, InetAddress ip_address) throws IOException{
            client = new Socket(ip_address, port);
            output = new DataOutputStream(client.getOutputStream());
        }

        @Override
        public void run(){
            try{
                //Scanner system_input = new Scanner(System.in);
                incoming = new IncomingData(client);
                incoming.start();
            }
            catch(IOException e){
                e.printStackTrace();
            }
        }


        //due to the way the server is set up, this method is needed to send a username since the server asks for a
        //username first, then uses that username as a key in a hashtable
        public void sendUserNameToServer(String userName){
            try {
                output.writeUTF(userName);
            }
            catch(IOException e){
                e.printStackTrace();
            }
        }

        //send a message to the server
        public void sendMessageToServer(String message){
            try {
                output.writeUTF(message);
            }
            catch(IOException e){
                e.printStackTrace();
                //return null;
            }
        }

    }

    class IncomingData extends Thread {
        private final Socket clientSocket;
        private final DataInputStream incoming;
        private String[] receiving;

        public IncomingData(Socket client) throws IOException{
            clientSocket = client;
            incoming = new DataInputStream(clientSocket.getInputStream());
        }


        //server prefaces every message with what is in it, and from there we figure out what to do
        @Override
        public void run(){
            while (true) {
                try{
                    receiving = incoming.readUTF().split(" "); //turn the message into an array

                    //check the first item in the message, as that says what's in it
                    if(receiving[0].equals("users")){
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                items.remove(0, items.size());
                                items.addAll(Arrays.copyOfRange(receiving,1, receiving.length));
                            }
                        });
                    }
                    else if(receiving[0].equals("move")){

                    }
                    else if(receiving[0].equals("invite")){
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                invites.setText("Invite from " + receiving[1]);
                            }
                        });
                    }
                    else if(receiving[0].equals("side")){
                        if(receiving[1].equals("black")){
                            side = false;
                        }
                        else{
                            side = true;
                        }
                    }
                }
                catch(IOException e){
                    e.printStackTrace();
                    break;
                }
            }
        }
    }


    private Parent createContent() {
        Pane root = new Pane();
        root.setPrefSize(WIDTH * TILE_SIZE, HEIGHT * TILE_SIZE);
        root.getChildren().addAll(tileGroup, pieceGroup);

        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                Tile tile = new Tile((x + y) % 2 == 0, x, y);
                board[x][y] = tile;

                tileGroup.getChildren().add(tile);

                Piece piece = null;

                if (y == 0) {
                    switch (x){
                        case 0:  piece = makePiece(PieceType.RED_ROOK_LIGHT, x, y, 0);
                            break;
                        case 1:  piece = makePiece(PieceType.RED_KNIGHT, x, y, 0);
                            break;
                        case 2:  piece = makePiece(PieceType.RED_BISHOP_LIGHT, x, y, 0);
                            break;
                        case 3:  piece = makePiece(PieceType.RED_QUEEN, x, y, 0);
                            break;
                        case 4: piece = makePiece(PieceType.RED_KING, x, y, 0);
                            break;
                        case 5: piece = makePiece(PieceType.RED_BISHOP_DARK, x, y, 0);
                            break;
                        case 6: piece = makePiece(PieceType.RED_KNIGHT, x, y, 0);
                            break;
                        case 7: piece = makePiece(PieceType.RED_ROOK_DARK, x, y, 0);
                            break;
                        default:
                    }
                }
                if (y == 1) {
                    piece = makePiece(PieceType.RED_PAWN, x, y, 0);
                }

                if (y == 7) {
                    switch (x){
                        case 0:  piece = makePiece(PieceType.WHITE_ROOK_DARK, x, y, 0);
                            break;
                        case 1:  piece = makePiece(PieceType.WHITE_KNIGHT, x, y, 0);
                            break;
                        case 2:  piece = makePiece(PieceType.WHITE_BISHOP_DARK, x, y, 0);
                            break;
                        case 3:  piece = makePiece(PieceType.WHITE_KING, x, y, 0);
                            break;
                        case 4: piece = makePiece(PieceType.WHITE_QUEEN, x, y, 0);
                            break;
                        case 5: piece = makePiece(PieceType.WHITE_BISHOP_LIGHT, x, y, 0);
                            break;
                        case 6: piece = makePiece(PieceType.WHITE_KNIGHT, x, y, 0);
                            break;
                        case 7: piece = makePiece(PieceType.WHITE_ROOK_LIGHT, x, y, 0);
                            break;
                        default:
                    }
                }
                if (y == 6) {
                    piece = makePiece(PieceType.WHITE_PAWN, x, y, 0);
                }

                if (piece != null) {
                    tile.setPiece(piece);
                    pieceGroup.getChildren().add(piece);
                }
            }
        }
        return root;
    }

    private Piece makePiece(PieceType type, int x, int y, int movement) {
        Piece piece = new Piece(type, x, y, movement);

        piece.setOnMouseReleased(e -> {
            int newX = toBoard(piece.getLayoutX());
            int newY = toBoard(piece.getLayoutY());

            MoveResult result;

            if (newX < 0 || newY < 0 || newX >= WIDTH || newY >= HEIGHT) {
                result = new MoveResult(MoveType.NONE);
            } else {
                result = tryMove(piece, newX, newY);
            }

            int x0 = toBoard(piece.getOldX());
            int y0 = toBoard(piece.getOldY());

            switch (result.getType()) {
                case NONE:
                    piece.abortMove();
                    break;
                case NORMAL:
                    piece.move(newX, newY);
                    board[x0][y0].setPiece(null);
                    board[newX][newY].setPiece(piece);
                    turn=!turn;
                    Check_turn_Results();
                    piece.setMovement();
                    break;
                case KILL:
                    piece.move(newX, newY);
                    board[x0][y0].setPiece(null);
                    board[newX][newY].setPiece(piece);

                    Piece otherPiece = result.getPiece();
                    board[toBoard(otherPiece.getOldX())][toBoard(otherPiece.getOldY())].setPiece(null);
                    pieceGroup.getChildren().remove(otherPiece);
                    turn=!turn;
                    Check_turn_Results();
                    piece.setMovement();
                    break;
            }
        });
        return piece;
    }

    private int toBoard(double pixel) {
        return (int)(pixel + TILE_SIZE / 2) / TILE_SIZE;
    }



    private MoveResult tryMove(Piece piece, int newX, int newY) {
        if(newX==piece.getOldX()/100 && newY ==piece.getOldY()/100){             //if no move
            return new MoveResult(MoveType.NONE);
        }
        if (piece.getType()==PieceType.RED_PAWN){
            if(turn==true || side==true){
                return new MoveResult(MoveType.NONE);
            }
            if(((newX==(int)(piece.getOldX())/100) && ((newY==(int)(piece.getOldY())/100+1)||
                    ((newY==(int)(piece.getOldY())/100+2)&&piece.getMovement()==0)))
                    &&board[newX][newY].hasPiece()==false
                    &&checkPathBlockageStraight(piece, newX, newY)){
                return new MoveResult(MoveType.NORMAL);
            }
//            if(((newX==(int)(piece.getOldX())/100) && (newY==(int)(piece.getOldY())/100+2))
//                    &&board[newX][newY].hasPiece()==false&&piece.getMovement()==0
//                    &&checkPathBlockageStraight(piece, newX, newY)){
//                Check_promotion_Results(piece);
//                return new MoveResult(MoveType.NORMAL);
//            }
            if(board[newX][newY].hasPiece() &&
                    (((newX==(int)(piece.getOldX())/100+1) && (newY==(int)(piece.getOldY())/100+1)) ||
                            ((newX==(int)(piece.getOldX())/100-1) &&  (newY==(int)(piece.getOldY())/100+1)))
                    &&checkPathBlockageStraight(piece, newX, newY)){
                return new MoveResult(MoveType.KILL, board[newX][newY].getPiece());
            }
            else{
                return new MoveResult(MoveType.NONE);
            }
        }
        if (piece.getType()==PieceType.WHITE_PAWN ){
            if(turn==false || side == false){
                return new MoveResult(MoveType.NONE);
            }
            if(((newX==(int)(piece.getOldX())/100) && ((newY==(int)(piece.getOldY())/100-1)||
                    ((newY==(int)(piece.getOldY())/100-2)&&piece.getMovement()==0)))
                    &&board[newX][newY].hasPiece()==false
                    &&checkPathBlockageStraight(piece, newX, newY)){
                Check_promotion_Results(piece);
                return new MoveResult(MoveType.NORMAL);
            }
            if(board[newX][newY].hasPiece()&&
                    (((newX==(int)(piece.getOldX())/100-1) && (newY==(int)(piece.getOldY())/100-1)) ||
                            ((newX==(int)(piece.getOldX())/100+1) && (newY==(int)(piece.getOldY())/100-1)))
                    &&checkPathBlockageStraight(piece, newX, newY)){
                return new MoveResult(MoveType.KILL, board[newX][newY].getPiece());
            }
            else{
                return new MoveResult(MoveType.NONE);
            }
        }
        if (piece.getType()==PieceType.RED_ROOK_LIGHT || piece.getType()==PieceType.RED_ROOK_DARK) {
            if(turn==true || side == true){
                return new MoveResult(MoveType.NONE);
            }
            if ((((newX == (int) (piece.getOldX()) / 100 )) ||
                    ((newY == (int) (piece.getOldY()) / 100)))
                    && board[newX][newY].hasPiece() == false
                    &&checkPathBlockageStraight(piece, newX, newY)) {
                return new MoveResult(MoveType.NORMAL);
            }
            if (board[newX][newY].hasPiece() &&
                    (((newX == (int) (piece.getOldX()) / 100 )) ||
                            ((newY == (int) (piece.getOldY()) / 100)))
                    &&checkPathBlockageStraight(piece, newX, newY)) {
                return new MoveResult(MoveType.KILL, board[newX][newY].getPiece());
            } else {
                return new MoveResult(MoveType.NONE);
            }
        }
        if (piece.getType()==PieceType.WHITE_ROOK_LIGHT || piece.getType()==PieceType.WHITE_ROOK_DARK) {
            if(turn==false || side==false){
                return new MoveResult(MoveType.NONE);
            }
            if ((((newX == (int) (piece.getOldX()) / 100 )) ||
                    ((newY == (int) (piece.getOldY()) / 100)))
                    && board[newX][newY].hasPiece() == false
                    &&checkPathBlockageStraight(piece, newX, newY)) {
                return new MoveResult(MoveType.NORMAL);
            }
            if (board[newX][newY].hasPiece() &&
                    (((newX == (int) (piece.getOldX()) / 100 )) ||
                            ((newY == (int) (piece.getOldY()) / 100)))
                    &&checkPathBlockageStraight(piece, newX, newY)) {
                return new MoveResult(MoveType.KILL, board[newX][newY].getPiece());
            } else {
                return new MoveResult(MoveType.NONE);
            }
        }

        if (piece.getType()==PieceType.RED_KING ){
            if(turn==true || side==true){
                return new MoveResult(MoveType.NONE);
            }
            if(((newY<=(int)(piece.getOldY())/100+1)&&(newY>=(int)(piece.getOldY())/100-1)) &&
                    ((newX<=(int)(piece.getOldX())/100+1)&&(newX>=(int)(piece.getOldX())/100-1))
                    &&board[newX][newY].hasPiece()==false
                    &&checkPathBlockageStraight(piece, newX, newY)){
                return new MoveResult(MoveType.NORMAL);
            }
            if(((newY<=(int)(piece.getOldY())/100+1)&&(newY>=(int)(piece.getOldY())/100-1)) &&
                    ((newX<=(int)(piece.getOldX())/100+1)&&(newX>=(int)(piece.getOldX())/100-1))
                    &&board[newX][newY].hasPiece()==true
                    &&checkPathBlockageStraight(piece, newX, newY)){
                return new MoveResult(MoveType.KILL, board[newX][newY].getPiece());
            }
            else{
                return new MoveResult(MoveType.NONE);
            }
        }
        if (piece.getType()==PieceType.WHITE_KING){
            if(turn==false || side == false){
                return new MoveResult(MoveType.NONE);
            }
            if(((newY<=(int)(piece.getOldY())/100+1)&&(newY>=(int)(piece.getOldY())/100-1)) &&
                    ((newX<=(int)(piece.getOldX())/100+1)&&(newX>=(int)(piece.getOldX())/100-1))
                    &&board[newX][newY].hasPiece()==false
                    &&checkPathBlockageStraight(piece, newX, newY)){
                return new MoveResult(MoveType.NORMAL);
            }
            if(((newY<=(int)(piece.getOldY())/100+1)&&(newY>=(int)(piece.getOldY())/100-1)) &&
                    ((newX<=(int)(piece.getOldX())/100+1)&&(newX>=(int)(piece.getOldX())/100-1))
                    &&board[newX][newY].hasPiece()==true
                    &&checkPathBlockageStraight(piece, newX, newY)){
                return new MoveResult(MoveType.KILL, board[newX][newY].getPiece());
            }
            else{
                return new MoveResult(MoveType.NONE);
            }
        }
        if (piece.getType()==PieceType.RED_BISHOP_LIGHT||piece.getType()==PieceType.RED_BISHOP_DARK){
            if(turn==true || side==true){
                return new MoveResult(MoveType.NONE);
            }
            if ((Math.abs(newX-piece.getOldX()/100) - Math.abs(newY-piece.getOldY()/100)==0)
                    && board[newX][newY].hasPiece() == false
                    &&checkPathBlockageDiagonal(piece, newX, newY)) {
                return new MoveResult(MoveType.NORMAL);
            }
            if ((Math.abs(newX-piece.getOldX()/100) - Math.abs(newY-piece.getOldY()/100)==0)
                    && board[newX][newY].hasPiece() == true
                    &&checkPathBlockageDiagonal(piece, newX, newY)) {
                return new MoveResult(MoveType.KILL, board[newX][newY].getPiece());
            } else {
                return new MoveResult(MoveType.NONE);
            }
        }

        if (piece.getType()==PieceType.WHITE_BISHOP_LIGHT||piece.getType()==PieceType.WHITE_BISHOP_DARK){
            if(turn==false || side==false){
                return new MoveResult(MoveType.NONE);
            }
            if ((Math.abs(newX-piece.getOldX()/100) - Math.abs(newY-piece.getOldY()/100)==0)
                    && board[newX][newY].hasPiece() == false
                    &&checkPathBlockageDiagonal(piece, newX, newY)) {
                return new MoveResult(MoveType.NORMAL);
            }
            if ((Math.abs(newX-piece.getOldX()/100) - Math.abs(newY-piece.getOldY()/100)==0)
                    && board[newX][newY].hasPiece() == true
                    &&checkPathBlockageDiagonal(piece, newX, newY)) {
                return new MoveResult(MoveType.KILL, board[newX][newY].getPiece());
            } else {
                return new MoveResult(MoveType.NONE);
            }
        }


        if (piece.getType()==PieceType.RED_QUEEN){
            if(turn==true || side==true){
                return new MoveResult(MoveType.NONE);
            }
            if (((Math.abs(newX-piece.getOldX()/100) - Math.abs(newY-piece.getOldY()/100)==0)||
                    ((((newX == (int) (piece.getOldX()) / 100 )) || ((newY == (int) (piece.getOldY()) / 100)))))
                    && board[newX][newY].hasPiece() == false
                    &&checkPathBlockageStraight(piece, newX, newY)
                    &&checkPathBlockageDiagonal(piece, newX, newY)) {
                return new MoveResult(MoveType.NORMAL);
            }
            if (((Math.abs(newX-piece.getOldX()/100) - Math.abs(newY-piece.getOldY()/100)==0)||
                    (board[newX][newY].hasPiece() &&
                            (((newX == (int) (piece.getOldX()) / 100 )) ||
                                    ((newY == (int) (piece.getOldY()) / 100)))))
                    && board[newX][newY].hasPiece() == true
                    &&checkPathBlockageStraight(piece, newX, newY)
                    &&checkPathBlockageDiagonal(piece, newX, newY)) {
                return new MoveResult(MoveType.KILL, board[newX][newY].getPiece());
            } else {
                return new MoveResult(MoveType.NONE);
            }
        }

        if (piece.getType()==PieceType.WHITE_QUEEN){
            if(turn==false || side==false){
                return new MoveResult(MoveType.NONE);
            }
            if (((Math.abs(newX-piece.getOldX()/100) - Math.abs(newY-piece.getOldY()/100)==0)||
                    ((((newX == (int) (piece.getOldX()) / 100 )) || ((newY == (int) (piece.getOldY()) / 100)))))
                    && board[newX][newY].hasPiece() == false
                    &&checkPathBlockageStraight(piece, newX, newY)
                    &&checkPathBlockageDiagonal(piece, newX, newY)) {
                return new MoveResult(MoveType.NORMAL);
            }
            if (((Math.abs(newX-piece.getOldX()/100) - Math.abs(newY-piece.getOldY()/100)==0)||
                    (board[newX][newY].hasPiece() &&
                            (((newX == (int) (piece.getOldX()) / 100 )) ||
                                    ((newY == (int) (piece.getOldY()) / 100)))))
                    && board[newX][newY].hasPiece() == true
                    &&checkPathBlockageStraight(piece, newX, newY)
                    &&checkPathBlockageDiagonal(piece, newX, newY)) {
                return new MoveResult(MoveType.KILL, board[newX][newY].getPiece());
            } else {
                return new MoveResult(MoveType.NONE);
            }
        }

        if (piece.getType()==PieceType.RED_KNIGHT) {
            if(turn==true || side==true){
                return new MoveResult(MoveType.NONE);
            }
            return getMoveResult_Knight(piece, newX, newY);
        }
        if (piece.getType()==PieceType.WHITE_KNIGHT) {
            if(turn==false || side==false){
                return new MoveResult(MoveType.NONE);
            }
            return getMoveResult_Knight(piece, newX, newY);
        }

        return new MoveResult(MoveType.NONE);
    }

    private MoveResult getMoveResult_Knight(Piece piece, int newX, int newY) {
        if((Math.abs(newX-piece.getOldX()/100) == 2)&&(Math.abs(newY-piece.getOldY()/100) == 1)){
            if(!board[newX][newY].hasPiece()){
                return new MoveResult(MoveType.NORMAL);
            }
            return new MoveResult(MoveType.KILL, board[newX][newY].getPiece());
        }
        else if(Math.abs(newY-piece.getOldY()/100)==2 && (Math.abs(newX-piece.getOldX()/100) == 1)){
            if(!board[newX][newY].hasPiece()){
                return new MoveResult(MoveType.NORMAL);
            }
            return new MoveResult(MoveType.KILL, board[newX][newY].getPiece());
        }
        else{
            return new MoveResult(MoveType.NONE);
        }
    }
    private boolean checkPathBlockageStraight(Piece piece, int newX, int newY){
        int curX = (int)piece.getOldX()/100;
        int curY = (int)piece.getOldY()/100;
        if(piece.getOldX()/100==newX){              //not moving hor
            if(piece.getOldY()/100>newY){           //moving vert    backwards
                for(int i =1; i < piece.getOldY()/100 - newY; i++){
                    curY--;
                    if(board[curX][curY].hasPiece()){
                        return false;
                    }
                }
            }
            if(piece.getOldY()/100<newY){           //moving vert     forward
                for(int i =1; i < newY-piece.getOldY()/100; i++){
                    curY++;
                    if(board[curX][curY].hasPiece()){
                        return false;
                    }
                }
            }
        }
        if(piece.getOldY()/100==newY){          //not moving vert
            if(piece.getOldX()/100>newX){       //moving hor    backwards
                for(int i =1; i < piece.getOldX()/100 - newX; i++){
                    curX--;
                    if(board[curX][curY].hasPiece()){
                        return false;
                    }
                }
            }
            if(piece.getOldX()/100<newX){       //moving hor    forward
                for(int i =1; i < newX-piece.getOldX()/100 ; i++){
                    curX++;
                    if(board[curX][curY].hasPiece()){
                        return false;
                    }
                }
            }
        }
        return true;
    }
    private boolean checkPathBlockageDiagonal(Piece piece, int newX, int newY){
        int curX = (int)piece.getOldX()/100;
        int curY = (int)piece.getOldY()/100;
        if(piece.getOldX()/100>newX && piece.getOldY()/100>newY){       //x back y back
            for(int i =1; i < piece.getOldX()/100-newX ; i++){
                curX--;
                curY--;
                if(board[curX][curY].hasPiece()){
                    return false;
                }
            }
        }
        if(piece.getOldX()/100>newX && piece.getOldY()/100<newY){       //x back y forward
            for(int i =1; i < piece.getOldX()/100-newX ; i++){
                curX--;
                curY++;
                if(board[curX][curY].hasPiece()){
                    return false;
                }
            }
        }
        if(piece.getOldX()/100<newX && piece.getOldY()/100>newY){       //x forward y back
            for(int i =1; i > newX-piece.getOldX()/100; i--){
                curX++;
                curY--;
                if(board[curX][curY].hasPiece()){
                    return false;
                }
            }
        }
        if(piece.getOldX()/100<newX && piece.getOldY()/100<newY){       //x forward y forward
            for(int i =1; i > newX-piece.getOldX()/100; i--){       //-1 so it can kill on landing?
                curX++;
                curY++;
                if(board[curX][curY].hasPiece()){
                    return false;
                }
            }
        }
        return true;
    }

    private void Check_promotion_Results(Piece piece){
        if(turn){
            if(piece.getOldY()/100==1){
                Promotion(piece);
            }
        }
        else{
            if(piece.getOldY()/100==7){
                Promotion(piece);
            }
        }
    }

    private static void Promotion(Piece piece){
        Stage promotion = new Stage();
        promotion.initModality(Modality.APPLICATION_MODAL);
        promotion.setTitle("WINNER!");
        promotion.setMinHeight(250);
        promotion.setMinWidth(250);
        Label label = new Label("Pick Promotion");
        Button knightButton = new Button("knight");
        Button bishopButton = new Button("Bishop");
        Button rookButton = new Button("Rook");
        Button queenButton = new Button("Queen");
        if(turn){
            knightButton.setOnAction(e -> piece.setPieceType(PieceType.WHITE_KNIGHT));
            if(piece.getOldX()/100%2==0){
                bishopButton.setOnAction(e -> piece.setPieceType(PieceType.WHITE_BISHOP_DARK));
            }
            else{
                bishopButton.setOnAction(e -> piece.setPieceType(PieceType.WHITE_BISHOP_LIGHT));
            }
            rookButton.setOnAction(e -> piece.setPieceType(PieceType.WHITE_ROOK_DARK));
            queenButton.setOnAction(e -> piece.setPieceType(PieceType.WHITE_QUEEN ));
        }
        else{
            knightButton.setOnAction(e -> piece.setPieceType(PieceType.RED_KNIGHT));
            if(piece.getOldX()/100%2==0){
                bishopButton.setOnAction(e -> piece.setPieceType(PieceType.RED_BISHOP_LIGHT));
            }
            else{
                bishopButton.setOnAction(e -> piece.setPieceType(PieceType.RED_BISHOP_DARK));
            }
            rookButton.setOnAction(e -> piece.setPieceType(PieceType.RED_ROOK_DARK));
            queenButton.setOnAction(e -> piece.setPieceType(PieceType.RED_QUEEN ));
        }
        VBox layout = new VBox(10);
        layout.getChildren().addAll(label, knightButton, bishopButton, rookButton, queenButton);
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout);
        promotion.setScene(scene);
        promotion.showAndWait();
    }

    private void Check_turn_Results(){
        String color="grey";
        boolean winner = true;
        for (int x = 0; x < board.length;x++){
            for (int y = 0; y < board[x].length;y++){
                if(turn){
                    color="Red";
                    if(board[x][y].getPiece()==(null)){
                        continue;
                    }
                    if(board[x][y].getPiece().getType().equals(PieceType.WHITE_KING)){
                        winner=false;
                        break;
                    }

                }
                else{
                    color="White";
                    if(board[x][y].getPiece()==(null)){
                        continue;
                    }
                    if(board[x][y].getPiece().getType().equals(PieceType.RED_KING)){
                        winner=false;
                        break;
                    }
                }
            }
            if(!winner){
                break;
            }
        }
        if(winner){
            Winner_Alert( color);
            System.exit(0);
        }
    }

    private static void Winner_Alert(String color){
        Stage winner_Alert = new Stage();
        winner_Alert.initModality(Modality.APPLICATION_MODAL);
        winner_Alert.setTitle("WINNER!");
        winner_Alert.setMinHeight(250);
        winner_Alert.setMinWidth(250);
        Label label = new Label(color+" Wins");
        Button closeButton = new Button("Close");

        closeButton.setOnAction(e -> winner_Alert.close());

        VBox layout = new VBox(10);
        layout.getChildren().addAll(label, closeButton);
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout);
        winner_Alert.setScene(scene);
        winner_Alert.showAndWait();
    }
}