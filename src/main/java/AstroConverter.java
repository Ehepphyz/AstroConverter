import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

// Importing the CDS HEALPix classes from your local project structure
import cds.healpix.Healpix;
import cds.healpix.HealpixNested;

public class AstroConverter extends JFrame {

    // Custom UI Colors
    private final Color BLUE_TITLE = new Color(0, 102, 204); 

    private JTextField txtInX, txtInY, txtEpoch, txtNSIDE;
    private JTextField txtOutCelRA, txtOutCelDec, txtOutEclL, txtOutEclB, txtOutGalL, txtOutGalB, txtOutSGL, txtOutSGB;
    private JTextField txtHealC, txtHealE, txtHealG, txtHealSG;
    private JComboBox<String> comboCoordIn;
    private JTextField txtOutAmerican, txtOutGMTString, txtOutJD, txtOutRJD, txtOutWMAP, txtOutTOD;

    public AstroConverter() {
        setupNativeLook();
        setTitle("Astro-Converter v1.0");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(5, 5));

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        mainPanel.add(createCoordinatePanel());
        mainPanel.add(Box.createRigidArea(new Dimension(0, 5))); 
        mainPanel.add(createTimePanel());

        add(new JScrollPane(mainPanel), BorderLayout.CENTER);
        
        this.setPreferredSize(new Dimension(380, 650)); 
        pack();
        setLocationRelativeTo(null);
    }

    private JPanel createCoordinatePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        
        TitledBorder title = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Coordinate Transformation");
        title.setTitleColor(BLUE_TITLE);
        title.setTitleFont(title.getTitleFont().deriveFont(Font.BOLD));
        panel.setBorder(title);
        
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(2, 4, 2, 4);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0; c.gridy = 0; c.weightx = 0; panel.add(new JLabel("Input Long / RA:"), c);
        c.gridx = 1; c.weightx = 1.0; txtInX = new JTextField("297.6958", 12); panel.add(txtInX, c);
        
        c.gridx = 0; c.gridy = 1; c.weightx = 0; panel.add(new JLabel("Input Lat / Dec:"), c);
        c.gridx = 1; c.weightx = 1.0; txtInY = new JTextField("8.8683", 12); panel.add(txtInY, c);

        c.gridx = 0; c.gridy = 2; panel.add(new JLabel("Source System:"), c);
        comboCoordIn = new JComboBox<>(new String[]{"Celestial", "Ecliptic", "Galactic", "Super Galactic"});
        c.gridx = 1; panel.add(comboCoordIn, c);

        c.gridx = 0; c.gridy = 3; panel.add(new JLabel("Epoch (J):"), c);
        txtEpoch = new JTextField("2000.0", 12);
        c.gridx = 1; panel.add(txtEpoch, c);

        c.gridx = 0; c.gridy = 4; panel.add(new JLabel("HEALPix NSIDE:"), c);
        txtNSIDE = new JTextField("512", 12);
        c.gridx = 1; panel.add(txtNSIDE, c);

        JButton btnCalc = new JButton("Calculate");
        btnCalc.addActionListener(this::handleCoordinateConversion);
        c.gridx = 0; c.gridy = 5; c.gridwidth = 2; panel.add(btnCalc, c);

        int row = 6;
        c.gridwidth = 1;
        txtOutCelRA  = addResultRow(panel, "Celestial RA:", row++);
        txtOutCelDec = addResultRow(panel, "Celestial Dec:", row++);
        txtOutEclL   = addResultRow(panel, "Ecliptic L:", row++);
        txtOutEclB   = addResultRow(panel, "Ecliptic B:", row++);
        txtOutGalL   = addResultRow(panel, "Galactic L:", row++);
        txtOutGalB   = addResultRow(panel, "Galactic B:", row++);
        txtOutSGL    = addResultRow(panel, "Super Galactic L:", row++);
        txtOutSGB    = addResultRow(panel, "Super Galactic B:", row++);

        // --- HEALPix Results Section ---
        JLabel lblHeal = new JLabel("HEALPix Nested Indices:");
        lblHeal.setForeground(BLUE_TITLE);
        lblHeal.setFont(lblHeal.getFont().deriveFont(Font.BOLD));
        c.gridy = row++; c.gridx = 0; panel.add(lblHeal, c);

        txtHealC  = addResultRow(panel, "Celestial Index:", row++);
        txtHealE  = addResultRow(panel, "Ecliptic Index:", row++);
        txtHealG  = addResultRow(panel, "Galactic Index:", row++);
        txtHealSG = addResultRow(panel, "Super Gal. Index:", row++);

        return panel;
    }

    private JTextField addResultRow(JPanel p, String label, int row) {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(1, 4, 1, 4); 
        c.gridy = row;
        c.gridx = 0; c.weightx = 0; p.add(new JLabel(label), c);
        c.gridx = 1; c.weightx = 1.0; c.fill = GridBagConstraints.HORIZONTAL;
        JTextField tf = new JTextField(12); 
        tf.setEditable(false); 
        tf.setBackground(new Color(245, 245, 245)); 
        p.add(tf, c);
        return tf;
    }

    private JPanel createTimePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        TitledBorder title = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Time Format Converter");
        title.setTitleColor(BLUE_TITLE);
        title.setTitleFont(title.getTitleFont().deriveFont(Font.BOLD));
        panel.setBorder(title);
        
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(2, 4, 2, 4);
        c.fill = GridBagConstraints.HORIZONTAL;

        JButton btnSync = new JButton("Sync with System Clock (UTC)");
        btnSync.addActionListener(e -> syncTime());
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2; panel.add(btnSync, c);

        c.gridwidth = 1;
        addTimeRow(panel, "U.S. Format:", txtOutAmerican = new JTextField(12), 1);
        addTimeRow(panel, "GMT String:", txtOutGMTString = new JTextField(12), 2);
        addTimeRow(panel, "Julian Day:", txtOutJD = new JTextField(12), 3);
        addTimeRow(panel, "Reduced JD:", txtOutRJD = new JTextField(12), 4);
        addTimeRow(panel, "WMAP RJD:", txtOutWMAP = new JTextField(12), 5);
        addTimeRow(panel, "WMAP TOD:", txtOutTOD = new JTextField(12), 6);

        return panel;
    }

    private void addTimeRow(JPanel p, String label, JTextField tf, int row) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = row; c.gridx = 0; c.weightx = 0; p.add(new JLabel(label), c);
        c.gridx = 1; c.weightx = 1.0; c.fill = GridBagConstraints.HORIZONTAL;
        tf.setEditable(false); 
        tf.setBackground(new Color(245, 245, 245));
        p.add(tf, c);
    }

    private void handleCoordinateConversion(ActionEvent e) {
        try {
            double inX = Double.parseDouble(txtInX.getText());
            double inY = Double.parseDouble(txtInY.getText());
            double epoch = Double.parseDouble(txtEpoch.getText());
            long nside = Long.parseLong(txtNSIDE.getText());

            // 1. NSIDE Validation
            if (nside <= 0 || (nside & (nside - 1)) != 0) {
                throw new Exception("NSIDE must be a power of 2 (e.g., 512, 1024).");
            }

            // 2. Calculate Obliquity for the given Epoch
            double t = (epoch - 2000.0) / 100.0;
            double eps = 23.4392911 - (46.8150 / 3600.0) * t; 

            String source = (String) comboCoordIn.getSelectedItem();
            double raJ2000, decJ2000;

            // 3. Normalize Input to Celestial J2000 Base
            if ("Galactic".equals(source)) {
                // Galactic is typically defined relative to J2000
                double[] cel = galacticToCelestial(inX, inY);
                raJ2000 = cel[0]; decJ2000 = cel[1];
            } else if ("Super Galactic".equals(source)) {
                double[] gal = superGalacticToGalactic(inX, inY);
                double[] cel = galacticToCelestial(gal[0], gal[1]);
                raJ2000 = cel[0]; decJ2000 = cel[1];
            } else if ("Ecliptic".equals(source)) {
                // Convert Ecliptic to Celestial at the current Epoch first
                double[] celAtEpoch = eclipticToCelestial(inX, inY, eps);
                // Reverse precess to J2000 (using negative time shift)
                double[] j2000 = precess(celAtEpoch[0], celAtEpoch[1], 2000.0);
                raJ2000 = j2000[0]; decJ2000 = j2000[1];
            } else {
                // Input is Celestial RA/Dec at the specified Epoch
                // Convert to J2000 base
                double[] j2000 = precess(inX, inY, 2000.0);
                raJ2000 = j2000[0]; decJ2000 = j2000[1];
            }

            // 4. Calculate Final Coordinates for the Target Epoch
            // RA/Dec at the target Epoch
            double[] targetCel = precess(raJ2000, decJ2000, epoch);
            double ra = targetCel[0];
            double dec = targetCel[1];

            // Derived systems based on precessed Celestial coordinates
            double[] gal  = celestialToGalactic(ra, dec);
            double[] sgal = galacticToSuperGalactic(gal[0], gal[1]);
            double[] ecl  = celestialToEcliptic(ra, dec, eps);

            // Update Coordinate UI
            txtOutCelRA.setText(String.format("%.6f", ra));
            txtOutCelDec.setText(String.format("%.6f", dec));
            txtOutGalL.setText(String.format("%.6f", gal[0]));
            txtOutGalB.setText(String.format("%.6f", gal[1]));
            txtOutSGL.setText(String.format("%.6f", sgal[0]));
            txtOutSGB.setText(String.format("%.6f", sgal[1]));
            txtOutEclL.setText(String.format("%.6f", ecl[0]));
            txtOutEclB.setText(String.format("%.6f", ecl[1]));

            // 5. CDS HEALPix Indices Calculation
            int order = (int) (Math.log(nside) / Math.log(2));
            HealpixNested hn = Healpix.getNested(order);

            // All library calls use Radians as required by CDS HEALPix
            txtHealC.setText(String.valueOf(hn.hash(Math.toRadians(ra), Math.toRadians(dec))));
            txtHealE.setText(String.valueOf(hn.hash(Math.toRadians(ecl[0]), Math.toRadians(ecl[1]))));
            txtHealG.setText(String.valueOf(hn.hash(Math.toRadians(gal[0]), Math.toRadians(gal[1]))));
            txtHealSG.setText(String.valueOf(hn.hash(Math.toRadians(sgal[0]), Math.toRadians(sgal[1]))));

        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Please enter valid numeric values.", "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // --- Transformation Math Functions ---

    private double[] celestialToEcliptic(double raDeg, double decDeg, double epsDeg) {
        double ra = Math.toRadians(raDeg), dec = Math.toRadians(decDeg), eps = Math.toRadians(epsDeg);
        double sinB = Math.sin(dec)*Math.cos(eps) - Math.cos(dec)*Math.sin(eps)*Math.sin(ra);
        double b = Math.asin(sinB);
        double l = Math.atan2(Math.sin(ra)*Math.cos(eps) + Math.tan(dec)*Math.sin(eps), Math.cos(ra));
        return new double[]{ (Math.toDegrees(l) + 360) % 360, Math.toDegrees(b) };
    }

    private double[] eclipticToCelestial(double lDeg, double bDeg, double epsDeg) {
        double l = Math.toRadians(lDeg), b = Math.toRadians(bDeg), eps = Math.toRadians(epsDeg);
        double sinDec = Math.sin(b)*Math.cos(eps) + Math.cos(b)*Math.sin(eps)*Math.sin(l);
        double dec = Math.asin(sinDec);
        double ra = Math.atan2(Math.sin(l)*Math.cos(eps) - Math.tan(b)*Math.sin(eps), Math.cos(l));
        return new double[]{ (Math.toDegrees(ra) + 360) % 360, Math.toDegrees(dec) };
    }

    private double[] celestialToGalactic(double raDeg, double decDeg) {
        // IAU J2000 high-precision constants
        double raNGP = Math.toRadians(192.85950833);
        double decNGP = Math.toRadians(27.12833611);
        double lNode = Math.toRadians(122.93191857); 

        double ra = Math.toRadians(raDeg);
        double dec = Math.toRadians(decDeg);

        // Galactic Latitude (b)
        double sinB = Math.sin(dec) * Math.sin(decNGP) + 
                    Math.cos(dec) * Math.cos(decNGP) * Math.cos(ra - raNGP);
        double b = Math.asin(Math.clamp(sinB, -1.0, 1.0));

        // Galactic Longitude (l)
        double y = Math.cos(dec) * Math.sin(ra - raNGP);
        double x = Math.sin(dec) * Math.cos(decNGP) - 
                Math.cos(dec) * Math.sin(decNGP) * Math.cos(ra - raNGP);
        
        double l = lNode - Math.atan2(y, x);
        
        return new double[]{(Math.toDegrees(l) + 360) % 360, Math.toDegrees(b)};
    }

    private double[] galacticToCelestial(double lDeg, double bDeg) {
        double l = Math.toRadians(lDeg), b = Math.toRadians(bDeg);
        double raP = Math.toRadians(192.85948), decP = Math.toRadians(27.12825), lcp = Math.toRadians(122.93192);
        double sinDec = Math.sin(decP)*Math.sin(b) + Math.cos(decP)*Math.cos(b)*Math.cos(lcp - l);
        double dec = Math.asin(sinDec);
        double ra = raP + Math.atan2(Math.cos(b)*Math.sin(lcp - l), Math.cos(decP)*Math.sin(b) - Math.sin(decP)*Math.cos(b)*Math.cos(lcp - l));
        return new double[]{(Math.toDegrees(ra) + 360) % 360, Math.toDegrees(dec)};
    }

    /**
     * Converts Galactic coordinates to Supergalactic coordinates.
     */
    private double[] galacticToSuperGalactic(double lDeg, double bDeg) {
        double l = Math.toRadians(lDeg);
        double b = Math.toRadians(bDeg);

        // 1. Cartesian Galactic
        double x = Math.cos(b) * Math.cos(l);
        double y = Math.cos(b) * Math.sin(l);
        double z = Math.sin(b);

        // 2. Apply Aladin's fromGalMatrix (Row-Major)
        // Row 1
        double xSG = (-0.7357425748043749 * x) + (0.6772612964138943 * y) + (0.0 * z);
        // Row 2
        double ySG = (-0.0745537783652337 * x) + (-0.0809914713069767 * y) + (0.9939225903997749 * z);
        // Row 3
        double zSG = (0.6731453021092076 * x) + (0.7312711658169645 * y) + (0.1100812622247821 * z);

        // 3. Spherical conversion
        double sgl = Math.atan2(ySG, xSG);
        double sgb = Math.asin(Math.clamp(zSG, -1.0, 1.0));

        double sglDeg = (Math.toDegrees(sgl) + 360.0) % 360.0;
        double sgbDeg = Math.toDegrees(sgb);

        return new double[]{ sglDeg, sgbDeg };
    }

    private double[] superGalacticToGalactic(double sglDeg, double sgbDeg) {
        double sgl = Math.toRadians(sglDeg);
        double sgb = Math.toRadians(sgbDeg);

        // 1. Supergalactic to Cartesian
        double xsg = Math.cos(sgb) * Math.cos(sgl);
        double ysg = Math.cos(sgb) * Math.sin(sgl);
        double zsg = Math.sin(sgb);

        // 2. Inverse Rotation Matrix
        double xg = -0.73574257 * xsg - 0.07451141 * ysg - 0.67314530 * zsg;
        double yg = -0.67726121 * xsg + 0.08092024 * ysg + 0.73127117 * zsg;
        double zg =  0.00000000 * xsg + 0.99392259 * ysg - 0.11008126 * zsg;

        // 3. Cartesian to Galactic
        double b = Math.asin(Math.clamp(zg, -1.0, 1.0));
        double l = Math.atan2(yg, xg);

        // Normalize l to [0, 360]
        double lOut = Math.toDegrees(l);
        if (lOut < 0) lOut += 360.0;

        return new double[]{lOut, Math.toDegrees(b)};
    }

    // --- Time Conversion Section ---

    private void syncTime() {
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        txtOutAmerican.setText(now.format(DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss")));
        String ddd = String.format("%03d", now.getDayOfYear());
        String msPart = String.format("%04d", (now.getNano() / 100000));
        txtOutGMTString.setText(now.format(DateTimeFormatter.ofPattern("yyyy")) + ddd + 
                                now.format(DateTimeFormatter.ofPattern("HHmmss")) + msPart + "000");
        double jd = calculateJD(now);
        txtOutJD.setText(String.format("%.10f", jd));
        txtOutRJD.setText(String.format("%.10f", jd - 2400000.5));
        txtOutWMAP.setText(String.format("%.10f", jd - 2450000.0));
        ZonedDateTime launch = ZonedDateTime.of(2001, 6, 30, 19, 46, 46, 0, ZoneOffset.UTC);
        txtOutTOD.setText("" + ChronoUnit.DAYS.between(launch, now));
    }

    private double calculateJD(ZonedDateTime time) {
        int y = time.getYear(), m = time.getMonthValue();
        double d = time.getDayOfMonth() + (time.getHour()/24.0) + (time.getMinute()/1440.0) + (time.getSecond()/86400.0);
        if (m <= 2) { y--; m += 12; }
        int a = y / 100, b = 2 - a + (a / 4);
        return (int)(365.25 * (y + 4716)) + (int)(30.6001 * (m + 1)) + d + b - 1524.5;
    }

    private void setupNativeLook() {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AstroConverter().setVisible(true));
    }

    private double[] precess(double raDeg, double decDeg, double epoch) {
        double t = (epoch - 2000.0) / 100.0;
        // Precession constants in arcseconds
        double zeta = (2306.2181 * t + 0.30188 * t * t + 0.017998 * t * t * t) / 3600.0;
        double z = (2306.2181 * t + 1.09468 * t * t + 0.018203 * t * t * t) / 3600.0;
        double theta = (2004.3109 * t - 0.42665 * t * t - 0.041833 * t * t * t) / 3600.0;

        double ra = Math.toRadians(raDeg + zeta);
        double dec = Math.toRadians(decDeg);
        double th = Math.toRadians(theta);
        double ra_z = Math.toRadians(z);

        // Spherical trigonometry for precession
        double A = Math.cos(dec) * Math.sin(ra);
        double B = Math.cos(th) * Math.cos(dec) * Math.cos(ra) - Math.sin(th) * Math.sin(dec);
        double C = Math.sin(th) * Math.cos(dec) * Math.cos(ra) + Math.cos(th) * Math.sin(dec);

        double raOut = Math.toDegrees(Math.atan2(A, B) + ra_z);
        double decOut = Math.toDegrees(Math.asin(C));

        return new double[]{(raOut + 360) % 360, decOut};
    }

    /**
     * Converts ICRS (RA/Dec) coordinates directly to Supergalactic coordinates.
     * This uses the official CDS Aladin 'fromICRSbase' rotation matrix.
     * 
     * @param raDeg  Right Ascension in degrees (J2000)
     * @param decDeg Declination in degrees (J2000)
     * @return double array containing {Supergalactic Longitude, Supergalactic Latitude}
     */
    public double[] icrsToSuperGalactic(double raDeg, double decDeg) {
        // 1. Convert input to radians
        double ra = Math.toRadians(raDeg);
        double dec = Math.toRadians(decDeg);

        // 2. Convert to Cartesian coordinates on a unit sphere
        double x = Math.cos(dec) * Math.cos(ra);
        double y = Math.cos(dec) * Math.sin(ra);
        double z = Math.sin(dec);

        // 3. Apply the Aladin "Master Matrix" from Supergal.java
        // This matrix combines (ICRS -> Galactic) and (Galactic -> Supergalactic)
        double xSG = ( 0.37501555570601915 * x) + ( 0.34135887185720824 * y) + ( 0.8618801851666389 * z);
        double ySG = (-0.8983204377254853 * x) + (-0.09572710025099692 * y) + ( 0.4287851600069993 * z);
        double zSG = ( 0.22887490937889644 * x) + (-0.9350456902643366 * y) + ( 0.27075049949149175 * z);

        // 4. Convert back to spherical coordinates
        double sgl = Math.atan2(ySG, xSG);
        double sgb = Math.asin(Math.clamp(zSG, -1.0, 1.0));

        // 5. Normalize results to degrees
        double sglDeg = (Math.toDegrees(sgl) + 360.0) % 360.0;
        double sgbDeg = Math.toDegrees(sgb);

        return new double[]{ sglDeg, sgbDeg };
    }
}