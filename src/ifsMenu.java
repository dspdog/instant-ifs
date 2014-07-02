import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;

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

    JSlider camPitchSpinner;
    JSlider camYawSpinner;
    JSlider camRollSpinner;
    //JSlider camScaleSpinner;

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

    public void addLabeledFileChooser(SpringLayout layout, String labelText, JPanel panel, double row, int col){
        int width = 51;
        int totalCols = 3;
        int spinnerLeft = 5 + col*width;
        int spinnerRight = 5 + col*width+ width;
        int labelToSpinner = -5;
        int vspace = 20;
        int topPad=5;

        JButton button = new JButton(labelText);

        layout.putConstraint(SpringLayout.WEST, button, spinnerLeft, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.EAST, button, spinnerRight, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.NORTH, button, (int)(topPad+vspace*row), SpringLayout.NORTH, panel);

        button.addActionListener(this);

        panel.add(button);
    }

    public <T extends JComponent> void addLabeled(T comp, SpringLayout layout, String labelText, JPanel panel, double row){
        int compLeft = 70;
        int compRight = -5;
        int compToSpinner = -5;
        int vspace = 20;
        int topPad=5;

        JLabel label = new JLabel(labelText);

        layout.putConstraint(SpringLayout.EAST, label, compToSpinner, SpringLayout.WEST, comp);
        layout.putConstraint(SpringLayout.WEST, comp, compLeft, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.EAST, comp, compRight, SpringLayout.EAST, panel);
        layout.putConstraint(SpringLayout.NORTH, comp, (int)(topPad+vspace*row), SpringLayout.NORTH, panel);
        layout.putConstraint(SpringLayout.NORTH, label, (int)(topPad+vspace*row), SpringLayout.NORTH, panel);

        try{
            ((JSlider)comp).addChangeListener(this);
        }catch (Exception e){}
        try{
            ((JComboBox)comp).addActionListener(this);
        }catch (Exception e){}
        try{
            ((JSpinner)comp).addChangeListener(this);
        }catch (Exception e){}
        try{
            ((JCheckBox)comp).addChangeListener(this);
        }catch (Exception e){}

        panel.add(label);
        panel.add(comp);
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

        addLabeled(xSpinner, layout, "X", panel, 1);
        addLabeled(ySpinner, layout, "Y", panel, 2);
        addLabeled(zSpinner, layout, "Z", panel, 3);
        addLabeled(scaleSpinner, layout, "Scale %", panel, 4);
        addLabeled(pitchSpinner, layout, "Pitch°", panel, 6);
        addLabeled(yawSpinner, layout, "Yaw°", panel, 7);

        panel.add(ptLabel);
    }

    public void setupPdfPropertiesPanel(JPanel panel){
        JLabel renderLabel = new JLabel(" PDF Properties");

        int topPad=5;

        SpringLayout layout = new SpringLayout();
        panel.setLayout(layout);
        layout.putConstraint(SpringLayout.NORTH, ptLabel, topPad, SpringLayout.NORTH, panel);

        addLabeledFileChooser(layout, "X", panel, 1, 0);
        addLabeledFileChooser(layout, "Y", panel, 1, 1);
        addLabeledFileChooser(layout, "Z", panel, 1, 2);
        addLabeled(pdfModeCombo, layout, "Mix", panel, 3);

        panel.addMouseMotionListener(this);

        panel.add(renderLabel);
    }

    public void setupCameraPropertiesPanel(JPanel panel){
        JLabel cameraLabel = new JLabel(" Camera Properties");

        camPitchSpinner = new JSlider();
        camYawSpinner = new JSlider();
        camRollSpinner = new JSlider();
        //camScaleSpinner = new JSlider();

        camPitchSpinner.setMaximum(360);
        camYawSpinner.setMaximum(360);
        camRollSpinner.setMaximum(360);
        //camScaleSpinner.setMaximum(100);

        int topPad=5;

        SpringLayout layout = new SpringLayout();
        panel.setLayout(layout);
        layout.putConstraint(SpringLayout.NORTH, ptLabel, topPad, SpringLayout.NORTH, panel);

        addLabeled(camPitchSpinner, layout, "Pitch", panel, 1);
        addLabeled(camYawSpinner, layout, "Yaw", panel, 2.35);
        addLabeled(camRollSpinner, layout, "Roll", panel, 3.7);
        //addLabeled(camScaleSpinner, layout, "Scale", panel, 5);

        panel.add(cameraLabel);
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

        String[] renderModeStrings = {volume.RenderMode.VOLUMETRIC.toString(), volume.RenderMode.PROJECT_ONLY.toString()};
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

        addLabeled(renderModeCombo, layout, "Mode", panel, 0.7);
        addLabeled(brightnessSpinner, layout, "Brightness", panel, 2);
        addLabeled(samplesSpinner, layout, "Dots/Frame", panel, 3);
        addLabeled(iterationsSpinner, layout, "Iterations", panel, 4);

        addLabeled(frameHoldCheck, layout, "Hold Frame", panel, 5.5);

        addLabeled(potentialSpinner, layout, "Blur", panel, 6.6);
        addLabeled(potentialCheck, layout, "Gaussian", panel, 7.6);
        addLabeled(thresholdSpinner, layout, "Threshold", panel, 9);
        addLabeled(thresholdCheck, layout, "Threshold", panel, 10);
        addLabeled(findEdgesCheck, layout, "Find Edges", panel, 11);

        addLabeled(delayCheck, layout, "Framelimit", panel, 12.5);
        addLabeled(delaySpinner, layout, "Wait X ms", panel, 13.6);

        panel.add(renderLabel);
    }

    public ifsMenu(Frame f, ifsys is, JPanel sideMenu){

        inited=false;

        myIfsSys = is;
        JPanel pointProperties = new JPanel();
        JPanel renderProperties = new JPanel();
        JPanel pdfProperties = new JPanel();
        JPanel cameraProperties = new JPanel();

        //SIDE MENU

        setupPointPropertiesPanel(pointProperties);
        setupRenderPropertiesPanel(renderProperties);
        setupPdfPropertiesPanel(pdfProperties);
        setupCameraPropertiesPanel(cameraProperties);

        SpringLayout sideMenuLayout = new SpringLayout();
        sideMenu.setLayout(sideMenuLayout);

        JSplitPane splitPaneBottom = new JSplitPane(JSplitPane.VERTICAL_SPLIT, cameraProperties, pdfProperties);
        JSplitPane splitPaneTop = new JSplitPane(JSplitPane.VERTICAL_SPLIT, pointProperties, renderProperties);

        JSplitPane splitPaneBig = new JSplitPane(JSplitPane.VERTICAL_SPLIT, splitPaneTop, splitPaneBottom);


        splitPaneTop.setOrientation(JSplitPane.VERTICAL_SPLIT);
        splitPaneTop.setDividerLocation(200);

        splitPaneBottom.setOrientation(JSplitPane.VERTICAL_SPLIT);
        splitPaneBottom.setDividerLocation(200);

        int padding=0;
        sideMenuLayout.putConstraint(SpringLayout.EAST, splitPaneBig, padding, SpringLayout.EAST, sideMenu);
        sideMenuLayout.putConstraint(SpringLayout.WEST, splitPaneBig, padding, SpringLayout.WEST, sideMenu);
        sideMenuLayout.putConstraint(SpringLayout.SOUTH, splitPaneBig, padding, SpringLayout.SOUTH, sideMenu);
        sideMenuLayout.putConstraint(SpringLayout.NORTH, splitPaneBig, padding, SpringLayout.NORTH, sideMenu);


        splitPaneBig.setDividerLocation(512);
        sideMenu.add(splitPaneBig);

        MenuBar menuBar;
        Menu renderMenu, shapeMenu, guidesMenu, viewMenu;

        menuBar = new MenuBar();
        renderMenu = new Menu("Render");
        shapeMenu = new Menu("Shape");
        guidesMenu = new Menu("Guides");

        //RENDER MENU
            CheckboxMenuItem aaButton = new CheckboxMenuItem("Anti-Aliasing"); //anti-aliasing toggle
            aaButton.setState(is.theVolume.antiAliasing);
            aaButton.addItemListener(this);
            renderMenu.add(aaButton);

        //SHAPE MENU
            CheckboxMenuItem autoScaleButton = new CheckboxMenuItem("AutoScale Points"); //autoscale toggle
            autoScaleButton.setState(is.shape.autoScale);
            autoScaleButton.addItemListener(this);
            shapeMenu.add(autoScaleButton);

            CheckboxMenuItem imgButton = new CheckboxMenuItem("PDF Samples"); //img samples toggle
            imgButton.setState(is.rp.usePDFSamples);
            imgButton.addItemListener(this);
            shapeMenu.add(imgButton);

        //GUIDES MENU
            CheckboxMenuItem infoButton = new CheckboxMenuItem("Info Box"); //info box toggle
            infoButton.setState(!is.rp.infoHidden);
            infoButton.addItemListener(this);
            guidesMenu.add(infoButton);

            menuBar.add(renderMenu);
            menuBar.add(shapeMenu);
            menuBar.add(guidesMenu);

            f.setMenuBar(menuBar);
            inited=true;
    }

    public void updateSideMenu(){
        myIfsSys.rp.limitParams();
        autoChange = true;
        ptLabel.setText(" Point " + myIfsSys.pointSelected + " Properties:");
        if(inited && System.currentTimeMillis()-lastUiChange>100){
            if(myIfsSys.pointSelected !=-1){
                xSpinner.setValue(myIfsSys.selectedPt.x);
                ySpinner.setValue(myIfsSys.selectedPt.y);
                zSpinner.setValue(myIfsSys.selectedPt.z);

                camPitchSpinner.setValue((int)myIfsSys.theVolume.camPitch + 180);
                camRollSpinner.setValue((int)myIfsSys.theVolume.camRoll + 180);
                camYawSpinner.setValue((int)myIfsSys.theVolume.camYaw + 180);
                //camScaleSpinner.setValue((int)myIfsSys.theVolume.camScale*10);

                scaleSpinner.setValue(myIfsSys.selectedPt.scale * 100);

                pitchSpinner.setValue(myIfsSys.selectedPt.rotationPitch/Math.PI*180);
                yawSpinner.setValue(myIfsSys.selectedPt.rotationYaw/Math.PI*180);

                brightnessSpinner.setValue(myIfsSys.rp.brightnessMultiplier);
                samplesSpinner.setValue(myIfsSys.rp.samplesPerFrame);
                iterationsSpinner.setValue(myIfsSys.rp.iterations);
                thresholdSpinner.setValue(myIfsSys.rp.threshold);
                potentialSpinner.setValue(myIfsSys.rp.potentialRadius);
                delaySpinner.setValue(myIfsSys.rp.postProcessPeriod);

                frameHoldCheck.setSelected(myIfsSys.rp.holdFrame);
                thresholdCheck.setSelected(myIfsSys.rp.usingThreshold);
                potentialCheck.setSelected(myIfsSys.rp.usingGaussian);
                findEdgesCheck.setSelected(myIfsSys.rp.usingFindEdges);
                delayCheck.setSelected(myIfsSys.rp.renderThrottling);
                //System.out.println(renderModeCombo.setse);
                renderModeCombo.setSelectedIndex(myIfsSys.theVolume.renderMode == volume.RenderMode.PROJECT_ONLY ? 1 : 0);
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
        //GUIDES MENU
            if(e.getItem()=="Info Box"){
                myIfsSys.rp.infoHidden = e.getStateChange()==2;
            }
        //SHAPE MENU
            if(e.getItem()=="AutoScale Points"){
                myIfsSys.shape.autoScale = e.getStateChange()==1;
            }
            if(e.getItem()=="PDF Samples"){
                myIfsSys.rp.usePDFSamples = e.getStateChange()==1;
            }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if(!autoChange){
            lastUiChange=System.currentTimeMillis();
            myIfsSys.selectedPt.x = (float)Double.parseDouble(xSpinner.getValue().toString());
            myIfsSys.selectedPt.y = (float)Double.parseDouble(ySpinner.getValue().toString());
            myIfsSys.selectedPt.z = (float)Double.parseDouble(zSpinner.getValue().toString());
            myIfsSys.selectedPt.scale = (float)(0.01 * Double.parseDouble(scaleSpinner.getValue().toString()));

            myIfsSys.selectedPt.rotationPitch = (float)(Double.parseDouble(pitchSpinner.getValue().toString())/180.0*Math.PI);
            myIfsSys.selectedPt.rotationYaw = (float)(Double.parseDouble(yawSpinner.getValue().toString())/180.0*Math.PI);

            myIfsSys.rp.usingThreshold = thresholdCheck.isSelected();
            myIfsSys.rp.threshold = Integer.parseInt(thresholdSpinner.getValue().toString());
            myIfsSys.rp.iterations = Integer.parseInt(iterationsSpinner.getValue().toString());
            myIfsSys.rp.brightnessMultiplier = Double.parseDouble(brightnessSpinner.getValue().toString());
            myIfsSys.rp.samplesPerFrame = Double.parseDouble(samplesSpinner.getValue().toString());

            myIfsSys.rp.renderThrottling = delayCheck.isSelected();
            myIfsSys.rp.postProcessPeriod = Long.parseLong(delaySpinner.getValue().toString());

            myIfsSys.rp.holdFrame = frameHoldCheck.isSelected();

            myIfsSys.rp.potentialRadius = Integer.parseInt(potentialSpinner.getValue().toString());
            myIfsSys.rp.usingGaussian = potentialCheck.isSelected();
            myIfsSys.rp.usingFindEdges = findEdgesCheck.isSelected();

            myIfsSys.shape.updateCenter();

            myIfsSys.theVolume.camPitch = camPitchSpinner.getValue() - 180;
            myIfsSys.theVolume.camYaw = camYawSpinner.getValue() - 180;
            myIfsSys.theVolume.camRoll = camRollSpinner.getValue() - 180;
            //myIfsSys.theVolume.camScale = camScaleSpinner.getValue()/10.0;

            //myIfsSys.theVolume.camScale = Math.max(0.1, myIfsSys.theVolume.camScale);

            myIfsSys.lastMoveTime = System.currentTimeMillis();

            if(!myIfsSys.rp.holdFrame)
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
                }else if(cb.getSelectedItem() == volume.RenderMode.PROJECT_ONLY.toString()){
                    myIfsSys.theVolume.renderMode = volume.RenderMode.PROJECT_ONLY;
                }else if(cb.getSelectedItem() == pdf3D.comboMode.ADD.toString()){
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

                if(cb.getText()=="X"){
                    pdfXImgFile = fc.showOpenDialog(this);
                    if(pdfXImgFile == JFileChooser.APPROVE_OPTION){
                        myIfsSys.thePdf.setSampleImageX(fc.getSelectedFile());
                    }
                }else if(cb.getText()=="Y"){
                    pdfYImgFile = fc.showOpenDialog(this);
                    if(pdfYImgFile == JFileChooser.APPROVE_OPTION){
                        myIfsSys.thePdf.setSampleImageY(fc.getSelectedFile());
                    }
                }else if(cb.getText()=="Z"){
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
