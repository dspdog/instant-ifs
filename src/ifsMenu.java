import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

public class ifsMenu extends Component implements ItemListener, ChangeListener, ActionListener, MouseMotionListener {

    ifsys myIfsSys;

    CheckboxMenuItem XYButton;
    CheckboxMenuItem XZButton;
    CheckboxMenuItem YZButton;

    JLabel ptLabel;
    JSpinner xSpinner;
    JSpinner ySpinner;
    JSpinner zSpinner;

    JSpinner brightnessSpinner;
    JSpinner samplesSpinner;
    JSpinner iterationsSpinner;
    JSpinner thresholdSpinner;
    JSpinner potentialSpinner;
    JSpinner delaySpinner;

    JSpinner pitchSpinner;
    JSpinner yawSpinner;

    JSpinner scaleSpinner;

    JCheckBox frameHoldCheck;
    JCheckBox thresholdCheck;
    JCheckBox potentialCheck;
    JCheckBox findEdgesCheck;

    JCheckBox delayCheck;

    JComboBox renderModeCombo;
    JComboBox pdfModeCombo;

    int pdfXImgFile = 0;
    int pdfYImgFile = 0;
    int pdfZImgFile = 0;

    final JFileChooser fc = new JFileChooser();

    boolean inited=false;
    boolean autoChange = false;
    long lastUiChange = 0;

    long lastPdfPropertiesMouseMoved=0;

    public void addLabeledFileChooser(JButton button, SpringLayout layout, String labelText, JPanel panel, double row){
        int spinnerLeft = 5;
        int spinnerRight = -5;
        int labelToSpinner = -5;
        int vspace = 20;
        int topPad=5;

        JLabel label = new JLabel(labelText);

        layout.putConstraint(SpringLayout.EAST, label, labelToSpinner, SpringLayout.WEST, button);
        layout.putConstraint(SpringLayout.WEST, button, spinnerLeft, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.EAST, button, spinnerRight, SpringLayout.EAST, panel);
        layout.putConstraint(SpringLayout.NORTH, button, (int)(topPad+vspace*row), SpringLayout.NORTH, panel);
        layout.putConstraint(SpringLayout.NORTH, label, (int)(topPad+vspace*row), SpringLayout.NORTH, panel);

        button.addActionListener(this);

        panel.add(label);
        panel.add(button);
    }

    public void addLabeledCombobox(JComboBox comboBox, SpringLayout layout, String labelText, JPanel panel, double row){
        int spinnerLeft = 70;
        int spinnerRight = -5;
        int labelToSpinner = -5;
        int vspace = 20;
        int topPad=5;

        JLabel label = new JLabel(labelText);

        layout.putConstraint(SpringLayout.EAST, label, labelToSpinner, SpringLayout.WEST, comboBox);
        layout.putConstraint(SpringLayout.WEST, comboBox, spinnerLeft, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.EAST, comboBox, spinnerRight, SpringLayout.EAST, panel);
        layout.putConstraint(SpringLayout.NORTH, comboBox, (int)(topPad+vspace*row), SpringLayout.NORTH, panel);
        layout.putConstraint(SpringLayout.NORTH, label, (int)(topPad+vspace*row), SpringLayout.NORTH, panel);

        comboBox.addActionListener(this);

        panel.add(label);
        panel.add(comboBox);
    }

    public void addLabeledSpinner(JSpinner spinner, SpringLayout layout, String labelText, JPanel panel, double row){
        int spinnerLeft = 70;
        int spinnerRight = -5;
        int labelToSpinner = -5;
        int vspace = 20;
        int topPad=5;

        JLabel label = new JLabel(labelText);

        layout.putConstraint(SpringLayout.EAST, label, labelToSpinner, SpringLayout.WEST, spinner);
        layout.putConstraint(SpringLayout.WEST, spinner, spinnerLeft, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.EAST, spinner, spinnerRight, SpringLayout.EAST, panel);
        layout.putConstraint(SpringLayout.NORTH, spinner, (int)(topPad+vspace*row), SpringLayout.NORTH, panel);
        layout.putConstraint(SpringLayout.NORTH, label, (int)(topPad+vspace*row), SpringLayout.NORTH, panel);

        spinner.addChangeListener(this);

        panel.add(label);
        panel.add(spinner);
    }

    public void addLabeledCheckbox(JCheckBox checkbox, SpringLayout layout, String labelText, JPanel panel, double row){
        int spinnerLeft = 70;
        int spinnerRight = -5;
        int labelToSpinner = -5;
        int vspace = 20;
        int topPad=5;

        JLabel label = new JLabel(labelText);

        layout.putConstraint(SpringLayout.EAST, label, labelToSpinner, SpringLayout.WEST, checkbox);
        layout.putConstraint(SpringLayout.WEST, checkbox, spinnerLeft, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.EAST, checkbox, spinnerRight, SpringLayout.EAST, panel);
        layout.putConstraint(SpringLayout.NORTH, checkbox, (int)(topPad+vspace*row), SpringLayout.NORTH, panel);
        layout.putConstraint(SpringLayout.NORTH, label, (int)(topPad+vspace*row), SpringLayout.NORTH, panel);

        checkbox.addChangeListener(this);

        panel.add(label);
        panel.add(checkbox);
    }

    public void setupPointPropertiesPanel(JPanel panel){
        xSpinner = new JSpinner();
        ySpinner = new JSpinner();
        zSpinner = new JSpinner();
        scaleSpinner = new JSpinner();

        pitchSpinner = new JSpinner();
        yawSpinner = new JSpinner();

        ptLabel = new JLabel("Point -1");

        int topPad=5;

        SpringLayout layout = new SpringLayout();
        panel.setLayout(layout);

        layout.putConstraint(SpringLayout.NORTH, ptLabel, topPad, SpringLayout.NORTH, panel);

        addLabeledSpinner(xSpinner, layout, "X", panel, 1);
        addLabeledSpinner(ySpinner, layout, "Y", panel, 2);
        addLabeledSpinner(zSpinner, layout, "Z", panel, 3);
        addLabeledSpinner(scaleSpinner, layout, "Scale %", panel, 4);
        addLabeledSpinner(pitchSpinner, layout, "Pitch°", panel, 6);
        addLabeledSpinner(yawSpinner, layout, "Yaw°", panel, 7);

        panel.add(ptLabel);
    }

    public void setupPdfPropertiesPanel(JPanel panel){
        JLabel renderLabel = new JLabel(" PDF Properties");

        int topPad=5;

        SpringLayout layout = new SpringLayout();
        panel.setLayout(layout);
        layout.putConstraint(SpringLayout.NORTH, ptLabel, topPad, SpringLayout.NORTH, panel);

        JButton xImgButton = new JButton("Choose X Img...");
        JButton yImgButton = new JButton("Choose Y Img...");
        JButton zImgButton = new JButton("Choose Z Img...");

        addLabeledFileChooser(xImgButton, layout, "", panel, 1);
        addLabeledFileChooser(yImgButton, layout, "", panel, 2.35);
        addLabeledFileChooser(zImgButton, layout, "", panel, 3.7);
        addLabeledCombobox(pdfModeCombo, layout, "Mix", panel, 6);

        panel.addMouseMotionListener(this);

        panel.add(renderLabel);
    }

    public void setupRenderPropertiesPanel(JPanel panel){
        brightnessSpinner = new JSpinner();
        samplesSpinner = new JSpinner();
        iterationsSpinner = new JSpinner();
        thresholdSpinner = new JSpinner();
        potentialSpinner = new JSpinner();
        delaySpinner = new JSpinner();

        frameHoldCheck = new JCheckBox();
        thresholdCheck = new JCheckBox();
        potentialCheck = new JCheckBox();
        findEdgesCheck = new JCheckBox();
        delayCheck = new JCheckBox();

        String[] renderModeStrings = {volume.RenderMode.VOLUMETRIC.toString(), volume.RenderMode.SIDES_ONLY.toString()};
        String[] pdfModeStrings = {
                                       pdf3D.comboMode.ADD.toString(),
                                       pdf3D.comboMode.AVERAGE.toString(),
                                       pdf3D.comboMode.MULTIPLY.toString(),
                                       pdf3D.comboMode.MAX.toString(),
                                       pdf3D.comboMode.MIN.toString(),
                                };

        renderModeCombo = new JComboBox(renderModeStrings);
        pdfModeCombo = new JComboBox(pdfModeStrings);

        JLabel renderLabel = new JLabel(" Render Properties");

        int topPad=5;

        SpringLayout layout = new SpringLayout();
        panel.setLayout(layout);

        layout.putConstraint(SpringLayout.NORTH, ptLabel, topPad, SpringLayout.NORTH, panel);

        addLabeledCombobox(renderModeCombo, layout, "Mode", panel,  0.7);
        addLabeledSpinner(brightnessSpinner, layout, "Brightness", panel, 2);
        addLabeledSpinner(samplesSpinner, layout, "Dots/Frame", panel, 3);
        addLabeledSpinner(iterationsSpinner, layout, "Iterations", panel, 4);

        addLabeledCheckbox(frameHoldCheck, layout, "Hold Frame", panel, 5.5);

        addLabeledSpinner(potentialSpinner, layout, "Blur", panel, 6.6);
        addLabeledCheckbox(potentialCheck, layout, "Gaussian", panel, 7.6);
        addLabeledSpinner(thresholdSpinner, layout, "Threshold", panel, 9);
        addLabeledCheckbox(thresholdCheck, layout, "Threshold", panel, 10);
        addLabeledCheckbox(findEdgesCheck, layout, "Find Edges", panel, 11);

        addLabeledCheckbox(delayCheck, layout, "Framelimit", panel, 12.5);
        addLabeledSpinner(delaySpinner, layout, "Wait X ms", panel, 13.6);

        panel.add(renderLabel);
    }

    public ifsMenu(Frame f, ifsys is, JPanel sideMenu){

        inited=false;

        myIfsSys = is;
        JPanel pointProperties = new JPanel();
        JPanel renderProperties = new JPanel();
        JPanel pdfProperties = new JPanel();

        //SIDE MENU

        setupPointPropertiesPanel(pointProperties);
        setupRenderPropertiesPanel(renderProperties);
        setupPdfPropertiesPanel(pdfProperties);

        SpringLayout sideMenuLayout = new SpringLayout();
        sideMenu.setLayout(sideMenuLayout);

        JSplitPane splitPaneBig = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

        JSplitPane splitPaneTop = new JSplitPane(JSplitPane.VERTICAL_SPLIT, pointProperties, renderProperties);
        splitPaneTop.setOrientation(JSplitPane.VERTICAL_SPLIT);
        splitPaneTop.setDividerLocation(200);

        int padding=0;
        sideMenuLayout.putConstraint(SpringLayout.EAST, splitPaneBig, padding, SpringLayout.EAST, sideMenu);
        sideMenuLayout.putConstraint(SpringLayout.WEST, splitPaneBig, padding, SpringLayout.WEST, sideMenu);
        sideMenuLayout.putConstraint(SpringLayout.SOUTH, splitPaneBig, padding, SpringLayout.SOUTH, sideMenu);
        sideMenuLayout.putConstraint(SpringLayout.NORTH, splitPaneBig, padding, SpringLayout.NORTH, sideMenu);


        splitPaneBig.setDividerLocation(512);
        splitPaneBig.setTopComponent(splitPaneTop);
        splitPaneBig.setBottomComponent(pdfProperties);
        sideMenu.add(splitPaneBig);

        MenuBar menuBar;
        Menu renderMenu, shapeMenu, guidesMenu, viewMenu;

        menuBar = new MenuBar();
        renderMenu = new Menu("Render");
        shapeMenu = new Menu("Shape");
        guidesMenu = new Menu("Guides");
        viewMenu = new Menu("View");

        //RENDER MENU
            CheckboxMenuItem aaButton = new CheckboxMenuItem("Anti-Aliasing"); //anti-aliasing toggle
            aaButton.setState(is.theVolume.antiAliasing);
            aaButton.addItemListener(this);
            renderMenu.add(aaButton);

        //VIEW MENU
            XYButton = new CheckboxMenuItem("XY");
            XYButton.addItemListener(this);
            viewMenu.add(XYButton);

            XZButton = new CheckboxMenuItem("XZ");
            XZButton.addItemListener(this);
            viewMenu.add(XZButton);

            YZButton = new CheckboxMenuItem("YZ");
            YZButton.addItemListener(this);
            viewMenu.add(YZButton);

            XYButton.setState(is.theVolume.preferredDirection == volume.ViewDirection.XY);
            XZButton.setState(is.theVolume.preferredDirection == volume.ViewDirection.XZ);
            YZButton.setState(is.theVolume.preferredDirection == volume.ViewDirection.YZ);

        //SHAPE MENU
            CheckboxMenuItem autoScaleButton = new CheckboxMenuItem("AutoScale Points"); //autoscale toggle
            autoScaleButton.setState(is.shape.autoScale);
            autoScaleButton.addItemListener(this);
            shapeMenu.add(autoScaleButton);

            CheckboxMenuItem imgButton = new CheckboxMenuItem("PDF Samples"); //img samples toggle
            imgButton.setState(is.usePDFSamples);
            imgButton.addItemListener(this);
            shapeMenu.add(imgButton);

        //GUIDES MENU
            CheckboxMenuItem infoButton = new CheckboxMenuItem("Info Box"); //info box toggle
            infoButton.setState(!is.infoHidden);
            infoButton.addItemListener(this);
            guidesMenu.add(infoButton);

            menuBar.add(renderMenu);
            menuBar.add(shapeMenu);
            menuBar.add(guidesMenu);
            menuBar.add(viewMenu);

            f.setMenuBar(menuBar);
            inited=true;
    }

    public void updateSideMenu(){
        autoChange = true;
        ptLabel.setText(" Point " + myIfsSys.pointSelected + " Properties:");
        if(inited && System.currentTimeMillis()-lastUiChange>100){
            if(myIfsSys.pointSelected !=-1){
                xSpinner.setValue(myIfsSys.selectedPt.x);
                ySpinner.setValue(myIfsSys.selectedPt.y);
                zSpinner.setValue(myIfsSys.selectedPt.z);
                scaleSpinner.setValue(myIfsSys.selectedPt.scale * 100);

                pitchSpinner.setValue(myIfsSys.selectedPt.rotationPitch/Math.PI*180);
                yawSpinner.setValue(myIfsSys.selectedPt.rotationYaw/Math.PI*180);

                brightnessSpinner.setValue(myIfsSys.brightnessMultiplier);
                samplesSpinner.setValue(myIfsSys.samplesPerFrame);
                iterationsSpinner.setValue(myIfsSys.iterations);
                thresholdSpinner.setValue(myIfsSys.threshold);
                potentialSpinner.setValue(myIfsSys.potentialRadius);
                delaySpinner.setValue(myIfsSys.postProcessPeriod);

                frameHoldCheck.setSelected(myIfsSys.holdFrame);
                thresholdCheck.setSelected(myIfsSys.usingThreshold);
                potentialCheck.setSelected(myIfsSys.usingGaussian);
                findEdgesCheck.setSelected(myIfsSys.usingFindEdges);
                delayCheck.setSelected(myIfsSys.renderThrottling);
                //System.out.println(renderModeCombo.setse);
                renderModeCombo.setSelectedIndex(myIfsSys.theVolume.renderMode == volume.RenderMode.SIDES_ONLY ? 1 : 0);
            }
        }

        switch (myIfsSys.thePdf.thePdfComboMode){
            case ADD:
                if(pdfModeCombo.getSelectedIndex()!=0)
                    pdfModeCombo.setSelectedIndex(0);
                break;
            case AVERAGE:
                if(pdfModeCombo.getSelectedIndex()!=1)
                    pdfModeCombo.setSelectedIndex(1);
                break;
            case MULTIPLY:
                if(pdfModeCombo.getSelectedIndex()!=2)
                    pdfModeCombo.setSelectedIndex(2);
                break;
            case MAX:
                if(pdfModeCombo.getSelectedIndex()!=3)
                    pdfModeCombo.setSelectedIndex(3);
                break;
            case MIN:
                if(pdfModeCombo.getSelectedIndex()!=4)
                    pdfModeCombo.setSelectedIndex(4);
                break;
        }

        autoChange = false;
    }

    public void itemStateChanged(ItemEvent e) {
        //RENDER MENU
            if(e.getItem()=="Anti-Aliasing"){
                myIfsSys.theVolume.antiAliasing = e.getStateChange()==1;
            }
        //VIEW MENU
            if(e.getItem()=="XY"){
                myIfsSys.theVolume.preferredDirection = volume.ViewDirection.XY;
                XYButton.setState(myIfsSys.theVolume.preferredDirection == volume.ViewDirection.XY);
                XZButton.setState(myIfsSys.theVolume.preferredDirection == volume.ViewDirection.XZ);
                YZButton.setState(myIfsSys.theVolume.preferredDirection == volume.ViewDirection.YZ);
            }
            if(e.getItem()=="XZ"){
                myIfsSys.theVolume.preferredDirection = volume.ViewDirection.XZ;
                XYButton.setState(myIfsSys.theVolume.preferredDirection == volume.ViewDirection.XY);
                XZButton.setState(myIfsSys.theVolume.preferredDirection == volume.ViewDirection.XZ);
                YZButton.setState(myIfsSys.theVolume.preferredDirection == volume.ViewDirection.YZ);
            }
            if(e.getItem()=="YZ"){
                myIfsSys.theVolume.preferredDirection = volume.ViewDirection.YZ;
                XYButton.setState(myIfsSys.theVolume.preferredDirection == volume.ViewDirection.XY);
                XZButton.setState(myIfsSys.theVolume.preferredDirection == volume.ViewDirection.XZ);
                YZButton.setState(myIfsSys.theVolume.preferredDirection == volume.ViewDirection.YZ);
            }

        //GUIDES MENU
            if(e.getItem()=="Info Box"){
                myIfsSys.infoHidden = e.getStateChange()==2;
            }
        //SHAPE MENU
            if(e.getItem()=="AutoScale Points"){
                myIfsSys.shape.autoScale = e.getStateChange()==1;
            }
            if(e.getItem()=="PDF Samples"){
                myIfsSys.usePDFSamples = e.getStateChange()==1;
            }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if(!autoChange){
            lastUiChange=System.currentTimeMillis();
            myIfsSys.selectedPt.x = Double.parseDouble(xSpinner.getValue().toString());
            myIfsSys.selectedPt.y = Double.parseDouble(ySpinner.getValue().toString());
            myIfsSys.selectedPt.z = Double.parseDouble(zSpinner.getValue().toString());
            myIfsSys.selectedPt.scale = 0.01 * Double.parseDouble(scaleSpinner.getValue().toString());

            myIfsSys.selectedPt.rotationPitch = Double.parseDouble(pitchSpinner.getValue().toString())/180.0*Math.PI;
            myIfsSys.selectedPt.rotationYaw = Double.parseDouble(yawSpinner.getValue().toString())/180.0*Math.PI;

            myIfsSys.usingThreshold = thresholdCheck.isSelected();
            myIfsSys.threshold = Integer.parseInt(thresholdSpinner.getValue().toString());
            myIfsSys.iterations = Integer.parseInt(iterationsSpinner.getValue().toString());
            myIfsSys.brightnessMultiplier = Double.parseDouble(brightnessSpinner.getValue().toString());
            myIfsSys.samplesPerFrame = Double.parseDouble(samplesSpinner.getValue().toString());

            myIfsSys.renderThrottling = delayCheck.isSelected();
            myIfsSys.postProcessPeriod = Long.parseLong(delaySpinner.getValue().toString());

            myIfsSys.holdFrame = frameHoldCheck.isSelected();

            myIfsSys.potentialRadius = Integer.parseInt(potentialSpinner.getValue().toString());
            myIfsSys.usingGaussian = potentialCheck.isSelected();
            myIfsSys.usingFindEdges = findEdgesCheck.isSelected();

            myIfsSys.shape.updateCenter();



            if(!myIfsSys.holdFrame)
            myIfsSys.clearframe();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) { //TODO do all this less hacky...
        if(!autoChange){
            //If coming from the combo boxs...
            try{
                JComboBox cb = (JComboBox)e.getSource();
                if(cb.getSelectedItem() == volume.RenderMode.VOLUMETRIC.toString()){
                    myIfsSys.theVolume.renderMode = volume.RenderMode.VOLUMETRIC;
                }else if(cb.getSelectedItem() == volume.RenderMode.SIDES_ONLY.toString()){
                    myIfsSys.theVolume.renderMode = volume.RenderMode.SIDES_ONLY;

                }else if(cb.getSelectedItem() == pdf3D.comboMode.ADD.toString()){
                    System.out.println("OK");
                    myIfsSys.thePdf.thePdfComboMode = pdf3D.comboMode.ADD;
                    myIfsSys.thePdf.updateVolume();
                }else if(cb.getSelectedItem() == pdf3D.comboMode.AVERAGE.toString()){
                    myIfsSys.thePdf.thePdfComboMode = pdf3D.comboMode.AVERAGE;
                    myIfsSys.thePdf.updateVolume();
                }else if(cb.getSelectedItem() == pdf3D.comboMode.MULTIPLY.toString()){
                    myIfsSys.thePdf.thePdfComboMode = pdf3D.comboMode.MULTIPLY;
                    myIfsSys.thePdf.updateVolume();
                }else if(cb.getSelectedItem() == pdf3D.comboMode.MAX.toString()){
                    myIfsSys.thePdf.thePdfComboMode = pdf3D.comboMode.MAX;
                    myIfsSys.thePdf.updateVolume();
                }else if(cb.getSelectedItem() == pdf3D.comboMode.MIN.toString()){
                    myIfsSys.thePdf.thePdfComboMode = pdf3D.comboMode.MIN;
                    myIfsSys.thePdf.updateVolume();
                }
            }catch (Exception ex){

            }

            //If coming from the pdf selections buttons...
            try{
                JButton cb = (JButton)e.getSource();

                if(cb.getText()=="Choose X Img..."){
                    pdfXImgFile = fc.showOpenDialog(this);
                    if(pdfXImgFile == JFileChooser.APPROVE_OPTION){
                        myIfsSys.thePdf.setSampleImageX(fc.getSelectedFile());
                    }
                }else if(cb.getText()=="Choose Y Img..."){
                    pdfYImgFile = fc.showOpenDialog(this);
                    if(pdfYImgFile == JFileChooser.APPROVE_OPTION){
                        myIfsSys.thePdf.setSampleImageY(fc.getSelectedFile());
                    }
                }else if(cb.getText()=="Choose Z Img..."){
                    pdfZImgFile = fc.showOpenDialog(this);
                    if(pdfZImgFile == JFileChooser.APPROVE_OPTION){
                        myIfsSys.thePdf.setSampleImageZ(fc.getSelectedFile());
                    }
                }
            }catch (Exception ex){

            }
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {
        //triggered by pdf properties panel

        lastPdfPropertiesMouseMoved=System.currentTimeMillis();
    }
}
