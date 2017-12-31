package com.junsung.wpi3;

import android.app.Activity;
import android.graphics.Color;
import android.hardware.Sensor;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.PointsGraphSeries;
import com.junsung.wpi3.MapElement.Edge;
import com.junsung.wpi3.MapElement.Junction;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


public class MainActivity extends Activity
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        MessageApi.MessageListener {

    //region Variables
    private TextView mDataView; // 시계로부터 전송받은 텍스트를 출력하는 뷰
    private TextView mSettingView; // 현재 세팅
    private TextView mStopGoView; // stop and go
    private TextView mCurrentCoordinateView; // current coordinate view

    static final int SIZE_OF_WINDOW = 5;

    static double[] sAccelSensorData = new double[SIZE_OF_WINDOW];
    private double mAccelThreshold = 10d;
    private double mXPoint = 0d, mYPoint = 0d;
    private double mDX = 0d, mDY = 0d;

    private EditText mKView;
    private double mKValue;

    // RadioButtonSensor
    private RadioButton mRBSRV;
    private RadioButton mRBSGRV;

    // RadioButtonCalibration
    private RadioButton mRBCSpeed;
    private RadioButton mRBCEasy;
    private RadioButton mRBCHard;
    private RadioButton mRBCGate;
    private RadioButton mRBCGRV;
    private RadioButton mRBCNone;

    private static final int VIEW_SIZE = 15;


    // Sensor Setting
    private int mSensorType;

    // flag for pause & auto
    private boolean mPauseFlag = true;
    private boolean mAutoFlag = false;

    private ArrayList<PointSet> mPointSets;
    private ArrayList<SensorDataSet> mSensorDataSets;

    // Junction for get SinC
    Junction startingPoint;
    Junction calibrationPoint;
    Junction C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, Rj;
    double calibrationValue;
    private boolean mGRVFlag = false;

    // graph init
    static LinearLayout sLinearLayout;
    static GraphView sGraphView;
    static PointsGraphSeries<DataPoint> sPointsSeries = new PointsGraphSeries<>();

    // graph flag
    // 0,0 0,1 0,2 = old
    // 1,0 1,1 1,2 = new
    private boolean[][] mGraphFlag = new boolean[2][3];

    // MapData and calibration
    private MapData mMapData;

    // 구글 플레이 서비스 API 객체
    private GoogleApiClient mGoogleApiClient;

    // save name
    private static String sName = "";

    //endregion

    //region Methods
    @Override // Activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 구글 플레이 서비스 객체를 시계 설정으로 초기화
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        // MapData 그래프
        startingPoint = new Junction(0, 0, 1);
        calibrationPoint = new Junction(5, 0, 1);
        C = new Junction(5, 5, 1);
        D = new Junction(7.5, 5, 1);
        E = new Junction(10, 5, 1);
        F = new Junction(12.5, 5, 1);
        G = new Junction(15, 5, 1);
        H = new Junction(5, 10, 1);
        I = new Junction(7.5, 10, 1);
        J = new Junction(10, 10, 1);
        K = new Junction(12.5, 10, 1);
        L = new Junction(15, 10, 1);
        M = new Junction(5, 15, 1);
        N = new Junction(7.5, 15, 1);
        O = new Junction(10, 15, 1);
        P = new Junction(12.5, 15, 1);
        Q = new Junction(15, 15, 1);
        Rj = new Junction(5, 20, 1);

        Edge AB = new Edge(startingPoint, calibrationPoint);
        Edge BC = new Edge(calibrationPoint, C); Edge BD = new Edge(calibrationPoint, D); Edge BE = new Edge(calibrationPoint, E);
        Edge BF = new Edge(calibrationPoint, F); Edge BG = new Edge(calibrationPoint, G);
        Edge CD = new Edge(C, D); Edge CH = new Edge(C, H);
        Edge DE = new Edge(D, E); Edge DI = new Edge(D, I);
        Edge EF = new Edge(E, F); Edge EJ = new Edge(E, J);
        Edge FG = new Edge(F, G); Edge FK = new Edge(F, K);
        Edge GL = new Edge(G, L);
        Edge HI = new Edge(H, I); Edge HM = new Edge(H, M);
        Edge IJ = new Edge(I, J); Edge IL = new Edge(I, L);
        Edge JK = new Edge(J, K); Edge JO = new Edge(J, O);
        Edge KL = new Edge(K, L); Edge KP = new Edge(K, P);
        Edge LQ = new Edge(L, Q);
        Edge ML = new Edge(M, L); Edge MR = new Edge(M, Rj);
        Edge LO = new Edge(L, O); Edge LR = new Edge(L, Rj);
        Edge OP = new Edge(O, P); Edge OR = new Edge(O, Rj);
        Edge PQ = new Edge(P, Q); Edge PR = new Edge(P, Rj);
        Edge QR = new Edge(Q, Rj);

        Junction[] junctions = {startingPoint, calibrationPoint, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, Rj};
        Edge[] edges = {AB, BC, BD, BE, BF, BG, CD, CH, DE, DI, EF, EJ, FG, FK, GL, HI, HM, IJ, IL,
                JK, JO, KL, KP, LQ, ML, MR, LO, LR, OP, OR, PQ, PR, QR};

        mMapData = new MapData(junctions, edges);

        graphInit();
        viewInit();

        mPointSets = new ArrayList<>();
        mSensorDataSets = new ArrayList<>();

        // 액티비티 화면 안꺼지도록
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // init threshold sensor data
        for(int i = 0 ; i < SIZE_OF_WINDOW; i++)
            sAccelSensorData[i] = 0d;
    }

    //region About Activity
    // 액티비티가 시작할 때 실행
    @Override // Activity
    protected void onStart() {
        super.onStart();

        // 구글 플레이 서비스에 접속돼 있지 않다면 접속한다.
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
    }

    // 액티비티가 종료될 때 실행
    @Override // Activity
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    public void onExit(View view) {
        moveTaskToBack(true);
        finish();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    //endregion

    //region About Google Connection
    // 구글 플레이 서비스에 접속 됐을 때 실행
    @Override // GoogleApiClient.ConnectionCallbacks
    public void onConnected(Bundle bundle) {
        Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();

        // 노드, 메시지, 데이터 이벤트를 활용할 수 있도록 이벤트 리스너 지정
        //2016.02.01. 구글에서 더이상 NodeApi는 사용을 권장하지 않음
        //Wearable.NodeApi.addListener(mGoogleApiClient, this);
        Wearable.MessageApi.addListener(mGoogleApiClient, this);
    }

    // 구글 플레이 서비스에 접속이 일시정지 됐을 때 실행
    @Override // GoogleApiClient.ConnectionCallbacks
    public void onConnectionSuspended(int i) {
        Toast.makeText(this, "Connection Suspended", Toast.LENGTH_SHORT).show();
    }

    // 구글 플레이 서비스에 접속을 실패했을 때 실행
    @Override // GoogleApiClient.OnConnectionFailedListener
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Connection Failed", Toast.LENGTH_SHORT).show();
    }

    // 시계로 데이터 및 메시지를 전송 후 실행되는 메소드
    private ResultCallback resultCallback = new ResultCallback() {
        @Override
        public void onResult(@NonNull Result result) {
            String resultString = "Sending Result : " + result.getStatus().isSuccess();

            Toast.makeText(getApplication(), resultString, Toast.LENGTH_SHORT).show();
        }
    };
    //endregion


    // 메시지가 수신되면 실행되는 메소드
    @Override // MessageApi.MessageListener
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getPath().equals("/MESSAGE_PATH")) {
            final String msg = new String(messageEvent.getData(), 0, messageEvent.getData().length);
            final String[] strSplit = msg.split(":::");
            final String finalMsg = String.valueOf(cutDouble(mKValue))+ "\n" +
                    String.valueOf(Double.parseDouble(strSplit[0])) + "  " +
                    String.valueOf(Double.parseDouble(strSplit[1])) + "  " +
                    String.valueOf(Double.parseDouble(strSplit[2])) + "\n" +
                    String.valueOf(Double.parseDouble(strSplit[3]));
            final double[] sensorData = new double[4];
            for(int i = 0 ; i < sensorData.length; i++)
                sensorData[i] = Double.parseDouble(strSplit[i]);

            moveWindow(sAccelSensorData, sensorData[3]);

            // UI 스레드를 실행하여 텍스트 뷰의 값을 수정한다.
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    boolean goingFlag = mAutoFlag || isGoing(sAccelSensorData, mAccelThreshold);

                   if (goingFlag && !mPauseFlag) {
                        mStopGoView.setText("GO");
                        mStopGoView.setBackgroundColor(Color.GREEN);

                        double headingDirection = (sensorData[0] + 180) % 360;

                        // calibration on
                        if (mGRVFlag) {
                            headingDirection = headingDirection + Math.toDegrees(calibrationValue);
                            headingDirection %= 360;
                        }

                        mDX = Math.sin(Math.toRadians(headingDirection));
                        mDY = Math.cos(Math.toRadians(headingDirection));
                        mXPoint += mDX;
                        mYPoint += mDY;

                        // point to metric
                        double metricX, metricY;
                        metricX = mXPoint * mKValue;
                        metricY = mYPoint * mKValue;

                        double dist = Math.sqrt(metricX * metricX + metricY * metricY);
                        mCurrentCoordinateView.setText("(" +
                                String.valueOf(cutDouble(metricX)) + ", " +
                                String.valueOf(cutDouble(metricY)) + ")" +
                                String.valueOf(cutDouble(dist))
                        );

                        sPointsSeries.appendData(new DataPoint(metricX, metricY), false, 10000);
//                        moveViewPort(sGraphView, metricX, metricY);
                        moveViewPort(sGraphView, 10, 10);

                        mMapData.update(cutDouble(metricX), cutDouble(metricY));

                        mPointSets.add(new PointSet(metricX, metricY));
                        mSensorDataSets.add(new SensorDataSet(sensorData));
                    } else {
                        mStopGoView.setText("STOP");
                        mStopGoView.setBackgroundColor(Color.RED);
                    }

                    mDataView.setText(finalMsg);

                    // refresh
                    sLinearLayout.removeView(sGraphView);
                    sLinearLayout.addView(sGraphView);
                }
            });
        }
    }


    //region About Button Event
    // Apply Message 버튼을 클릭했을 때 실행
    public void onApplySetting(View view) {

        // 페어링 기기들을 지칭하는 노드를 가져온다.
        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient)
                .setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {

                    // 노드를 가져온 후 실행된다.
                    @Override
                    public void onResult(@NonNull NodeApi.GetConnectedNodesResult
                                                 getConnectedNodesResult) {

                        // 노드를 순회하며 메시지를 전송한다.
                        for (final Node node : getConnectedNodesResult.getNodes()) {

                            // 전송할 메시지 텍스트 생성
                            String message = String.valueOf(mSensorType);
                            byte[] bytes = message.getBytes();

                            // 메시지 전송 및 전송 후 실행 될 콜백 함수 지정
                            Wearable.MessageApi.sendMessage(mGoogleApiClient,
                                    node.getId(), "/MESSAGE_PATH", bytes)
                                    .setResultCallback(resultCallback);
                        }
                    }
                });

        mKValue = Double.parseDouble(mKView.getText().toString());
    }

    // 시계 작동을 핸드폰에서 실행
    public void onCaptureWear(View view) {
        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient)
                .setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                    @Override
                    public void onResult(@NonNull NodeApi.GetConnectedNodesResult
                                                 getConnectedNodesResult) {
                        for(final Node node : getConnectedNodesResult.getNodes()) {
                            String message = "capture";
                            byte[] bytes = message.getBytes();

                            Wearable.MessageApi.sendMessage(mGoogleApiClient,
                                    node.getId(), "/CONTROL_PATH", bytes)
                                    .setResultCallback(resultCallback);
                        }
                    }
                });
    }

    public void onCalibrate(View view) {
        if(mRBCSpeed.isChecked()) {
            sName = "speed";
            mMapData.user.lastJunction = calibrationPoint;
            double actualX = mMapData.user.x;
            double actualY = mMapData.user.y;
            double targetX = calibrationPoint.x;
            double targetY = calibrationPoint.y;

            mKValue = Math.sqrt(Math.pow(targetX, 2.0) + Math.pow(targetY, 2.0)) /
                    Math.sqrt(Math.pow(actualX, 2.0) + Math.pow(actualY, 2.0)) / 10;

            mMapData.update(targetX, targetY);
            mXPoint = targetX / mKValue;
            mYPoint = targetY / mKValue;

            mMapData.user.lastJunction = calibrationPoint;

            Toast.makeText(this, "Speed Done", Toast.LENGTH_SHORT).show();
        } else if(mRBCEasy.isChecked()) {
            sName = "easy";
            mMapData.correction(sName);

            mXPoint = mMapData.user.x / mKValue;
            mYPoint = mMapData.user.y / mKValue;


            Toast.makeText(this, "Easy Done", Toast.LENGTH_SHORT).show();
        } else if(mRBCHard.isChecked()) {
            sName = "hard";
            mMapData.correction(sName);

            mXPoint = mMapData.user.x / mKValue;
            mYPoint = mMapData.user.y / mKValue;
            Toast.makeText(this, "Hard Done", Toast.LENGTH_SHORT).show();
        } else if(mRBCGate.isChecked()) {
            sName = "gate";
            mMapData.correction(sName);

            mXPoint = mMapData.user.x / mKValue;
            mYPoint = mMapData.user.y / mKValue;
            Toast.makeText(this, "Gate Done", Toast.LENGTH_SHORT).show();
        } else if(mRBCGRV.isChecked()) {
            if(!mGRVFlag) {
                sName = "GRV";
                double actualX = mMapData.user.x;
                double actualY = mMapData.user.y;
                double targetX = calibrationPoint.x;
                double targetY = calibrationPoint.y;

                mKValue = Math.sqrt(Math.pow(targetX, 2.0) + Math.pow(targetY, 2.0)) /
                        Math.sqrt(Math.pow(actualX, 2.0) + Math.pow(actualY, 2.0)) / 10;

                calibrationValue = Math.asin(targetX / Math.sqrt(Math.pow(targetX, 2.0) + Math.pow(targetY, 2.0))) -
                        Math.asin(actualX / Math.sqrt(Math.pow(actualX, 2.0) + Math.pow(actualY, 2.0)));

                mMapData.update(targetX, targetY);
                mXPoint = targetX / mKValue;
                mYPoint = targetY / mKValue;

                mGRVFlag = true;
                Toast.makeText(this, "GRV Done", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Fail: GRV already done", Toast.LENGTH_SHORT).show();
            }
        } else if (mRBCNone.isChecked()) {
            sName = "none";
        }
    }


    public void onSave(View view) {
        String mStrPath, mStrSensor;
        File mPathFile, mSensorFile;

        mStrPath = Environment.getExternalStorageDirectory()
                .getAbsolutePath()
                + "/"
                + sName
                + new SimpleDateFormat("yyyyMMddHHmmss", Locale.KOREA).
                        format(new Date()) +
                "_path.txt";

        mPathFile = new File(mStrPath);

        int pointSetLength = mPointSets.size();
        for(int i = 0 ; i < pointSetLength; i++) {
            String tmp = String.valueOf(mPointSets.get(i).x) + "\t"
                    + String.valueOf(mPointSets.get(i).y);
            writeLog(mPathFile, tmp);
        }

        mStrSensor = Environment.getExternalStorageDirectory()
                .getAbsolutePath()
                + "/"
                + sName
                + new SimpleDateFormat("yyyyMMddHHmmss", Locale.KOREA).
                        format(new Date()) +
                "_sensor.txt";

        mSensorFile = new File(mStrSensor);

        int sensorDataLength = mSensorDataSets.size();
        for(int i = 0 ; i < sensorDataLength; i++) {
            String tmp = String.valueOf(mSensorDataSets.get(i).sensorData[0]) + "\t"
                    + String.valueOf(mSensorDataSets.get(i).sensorData[1]) + "\t"
                    + String.valueOf(mSensorDataSets.get(i).sensorData[2]);
            writeLog(mSensorFile, tmp);
        }


        Toast.makeText(getApplicationContext(), "File save success", Toast.LENGTH_SHORT).show();
    }
    //endregion

    //region About initializing
    private void graphInit() {
        sLinearLayout = findViewById(R.id.graphLayout);
        sPointsSeries = new PointsGraphSeries<>(new DataPoint[]{new DataPoint(0, 0)});
        sPointsSeries.setTitle("Path");
        sPointsSeries.setColor(Color.BLACK);
        sPointsSeries.setSize(5f);
        sGraphView = new GraphView(this);
        sGraphView.getViewport().setScalable(true);
        sGraphView.getViewport().setScrollable(true);
        sGraphView.getViewport().setMaxY(VIEW_SIZE);
        sGraphView.getViewport().setMinY(-1 * VIEW_SIZE);
        sGraphView.getViewport().setMaxX(VIEW_SIZE);
        sGraphView.getViewport().setMinX(-1 * VIEW_SIZE);
        sGraphView.getViewport().setYAxisBoundsManual(true);
        sGraphView.getLegendRenderer().setVisible(true);
        sGraphView.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.BOTTOM);
        sGraphView.addSeries(sPointsSeries);
        sLinearLayout.addView(sGraphView);
        for (int i = 0; i < 3; i++) {
            mGraphFlag[0][i] = true;
            mGraphFlag[1][i] = true;
        }

        sPointsSeries.appendData(new DataPoint(startingPoint.x, startingPoint.y), false, 10000);
        sPointsSeries.appendData(new DataPoint(calibrationPoint.x, calibrationPoint.y), false, 10000);
        sPointsSeries.appendData(new DataPoint(C.x, C.y), false, 10000);
        sPointsSeries.appendData(new DataPoint(D.x, D.y), false, 10000);
        sPointsSeries.appendData(new DataPoint(E.x, E.y), false, 10000);
        sPointsSeries.appendData(new DataPoint(F.x, F.y), false, 10000);
        sPointsSeries.appendData(new DataPoint(G.x, G.y), false, 10000);
        sPointsSeries.appendData(new DataPoint(H.x, H.y), false, 10000);
        sPointsSeries.appendData(new DataPoint(I.x, I.y), false, 10000);
        sPointsSeries.appendData(new DataPoint(J.x, J.y), false, 10000);
        sPointsSeries.appendData(new DataPoint(K.x, K.y), false, 10000);
        sPointsSeries.appendData(new DataPoint(L.x, L.y), false, 10000);
        sPointsSeries.appendData(new DataPoint(M.x, M.y), false, 10000);
        sPointsSeries.appendData(new DataPoint(N.x, N.y), false, 10000);
        sPointsSeries.appendData(new DataPoint(O.x, O.y), false, 10000);
        sPointsSeries.appendData(new DataPoint(P.x, P.y), false, 10000);
        sPointsSeries.appendData(new DataPoint(Q.x, Q.y), false, 10000);
        sPointsSeries.appendData(new DataPoint(Rj.x, Rj.y), false, 10000);
        moveViewPort(sGraphView, 10, 10);
    }

    private void viewInit() {
        // 시계로부터 전송받은 텍스트뷰
        mDataView = findViewById(R.id.dataView);

        // 현재 세팅
        mSettingView = findViewById(R.id.settingView);

        // stop and go textview
        mStopGoView = findViewById(R.id.stopOrGoView);
        mStopGoView.setText("STOP");
        mStopGoView.setBackgroundColor(Color.RED);

        // current coordinate view
        mCurrentCoordinateView = findViewById(R.id.currentCoordinateView);
        mCurrentCoordinateView.setText("(" +
                String.valueOf(cutDouble(mXPoint)) + ", " +
                String.valueOf(cutDouble(mYPoint)) + ")");

        // RadioButtonSensor
        mRBSRV = findViewById(R.id.sensorRV);
        mRBSRV.setOnClickListener(RBOnClickListener);
        mRBSGRV = findViewById(R.id.sensorGRV);
        mRBSGRV.setOnClickListener(RBOnClickListener);

        // RadioButtonCalibration
        mRBCSpeed = findViewById(R.id.caliSpeed);
        mRBCSpeed.setOnClickListener(RBOnClickListener);
        mRBCEasy = findViewById(R.id.caliEasy);
        mRBCEasy.setOnClickListener(RBOnClickListener);
        mRBCHard = findViewById(R.id.caliHard);
        mRBCHard.setOnClickListener(RBOnClickListener);
        mRBCGate = findViewById(R.id.caliGate);
        mRBCGate.setOnClickListener(RBOnClickListener);
        mRBCGRV = findViewById(R.id.caliGRV);
        mRBCGRV.setOnClickListener(RBOnClickListener);
        mRBCNone = findViewById(R.id.caliNone);
        mRBCNone.setOnClickListener(RBOnClickListener);

        // RadioButton init
        mRBSRV.setChecked(true);
        mRBCSpeed.setChecked(true);

        mKView = findViewById(R.id.kValueView);
        mKValue = Double.parseDouble(mKView.getText().toString());

        Switch mPauseSwitch = findViewById(R.id.switchPause);
        mPauseSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                mPauseFlag = isChecked;
            }
        });

        Switch mAutoSwitch = findViewById(R.id.switchAuto);
        mAutoSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                mAutoFlag = isChecked;
            }
        });
    }
    //endregion

    //region About RadioButton
    RadioButton.OnClickListener RBOnClickListener = new RadioButton.OnClickListener() {
        @Override
        public void onClick(View v) {
            String sensorTypeStr = null;

            if (mRBSRV.isChecked()) {
                mSensorType = Sensor.TYPE_ROTATION_VECTOR;
                sensorTypeStr = "RV";
            } else if (mRBSGRV.isChecked()) {
                mSensorType = Sensor.TYPE_GAME_ROTATION_VECTOR;
                sensorTypeStr = "GRV";
            }

            mSettingView.setText(sensorTypeStr);
        }
    };
    //endregion


    /**
     * 파일에 문자열을 적는 메소드
     *
     * @param file 문자열을 write 할 파일 객체
     * @param str 적을 문자열
     */
    private void writeLog(File file, String str) {
        if (file.exists()) {
        } else {
            try {
                file.createNewFile();
                PrintWriter printWriter = new PrintWriter(new BufferedWriter(new FileWriter(file)));

                printWriter.print("file writing start\n");

                printWriter.flush();
                printWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file, true)));
            pw.write(str + "\n");
            pw.flush();
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isGoing(double[] sensorData, double threshold) {
        boolean flag = false;

        for (double aSensorData : sensorData) {
            if (aSensorData > threshold) flag = true;
        }

        return flag;
    }

    private void moveWindow(double[] sensorData, double newData) {
        int dataLength = sensorData.length;

        System.arraycopy(sensorData, 1, sensorData, 0, dataLength - 1);

        sensorData[dataLength-1] = newData;
    }

    private void moveViewPort(GraphView graphView, double xPoint, double yPoint) {
        graphView.getViewport().setMinX(xPoint-VIEW_SIZE);
        graphView.getViewport().setMaxX(xPoint+VIEW_SIZE);
        graphView.getViewport().setMinY(yPoint-VIEW_SIZE);
        graphView.getViewport().setMaxY(yPoint+VIEW_SIZE);
    }

    // 소수점 2번? 자리에서 자르는 메소드
    public double cutDouble(double target) {
        String s = String.format("%.2f", target);
        return Double.parseDouble(s);
    }
    //endregion
}
