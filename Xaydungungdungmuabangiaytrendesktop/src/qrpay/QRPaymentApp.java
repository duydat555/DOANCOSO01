package qrpay;

import com.google.gson.Gson;
import okhttp3.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

public class QRPaymentApp extends JFrame {
    private JTextField amountField;
    private JLabel qrCodeLabel;

    public QRPaymentApp() {
        setTitle("QR Payment App");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 800);
        setLocationRelativeTo(null);
        initComponents();
    }

    private void initComponents() {
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(7, 2, 5, 5));

        inputPanel.add(new JLabel("Amount:"));
        amountField = new JTextField();
        inputPanel.add(amountField);

        JButton generateButton = new JButton("Generate QR");
        generateButton.addActionListener(e -> generateQRCode());
        inputPanel.add(generateButton);

        qrCodeLabel = new JLabel();
        qrCodeLabel.setHorizontalAlignment(JLabel.CENTER);

        setLayout(new BorderLayout());
        add(inputPanel, BorderLayout.NORTH);
        add(qrCodeLabel, BorderLayout.CENTER);
    }

    

    private void generateQRCode() {
        String accountNo = "0979487360";  
        String accountName = "LE DUY DAT";
        String amount = "69000";
        String addInfo = "THANH TOAN HOA DON";
        String template = "qr_only";
        String bank = "(970422) MBBank";
        String bankBin = bank.substring(1, bank.indexOf(')'));

        ApiRequest apiRequest = new ApiRequest(accountNo, accountName, Integer.parseInt(bankBin), Integer.parseInt(amount), addInfo, "text", template);
        String jsonRequest = new Gson().toJson(apiRequest);
        
        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(jsonRequest, JSON);
        Request request = new Request.Builder()
                .url("https://api.vietqr.io/v2/generate")
                .post(body)
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                String jsonResponse = response.body().string();
                ApiResponse apiResponse = new Gson().fromJson(jsonResponse, ApiResponse.class);
                String qrDataURL = apiResponse.getData().getQrDataURL().replace("data:image/png;base64,", "");
                ImageIcon qrImage = new ImageIcon(Base64ToImage(qrDataURL));
                qrImage = new ImageIcon(qrImage.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH)); 
                qrCodeLabel.setIcon(qrImage);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to generate QR code", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private BufferedImage Base64ToImage(String base64String) {
        try {
            byte[] imageBytes = java.util.Base64.getDecoder().decode(base64String);
            ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
            return ImageIO.read(bis);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            QRPaymentApp app = new QRPaymentApp();
            app.setVisible(true);
        });
    }
}

class ApiRequest {
    private String accountNo;
    private String accountName;
    private int acqId;
    private int amount;
    private String addInfo;
    private String format;
    private String template;

    public ApiRequest(String accountNo, String accountName, int acqId, int amount, String addInfo, String format, String template) {
        this.accountNo = accountNo;
        this.accountName = accountName;
        this.acqId = acqId;
        this.amount = amount;
        this.addInfo = addInfo;
        this.format = format;
        this.template = template;
    }
}

class ApiResponse {
    private String code;
    private String desc;
    private Data data;

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public Data getData() {
        return data;
    }

    class Data {
        private String qrDataURL;

        public String getQrDataURL() {
            return qrDataURL;
        }
    }
}

class BankResponse {
    private String code;
    private String desc;
    private List<BankData> data;

    public List<BankData> getData() {
        return data;
    }
}

class BankData {
    private String bin;
    private String shortName;

    public String getCustomName() {
        return String.format("(%s) %s", bin, shortName);
    }
}
