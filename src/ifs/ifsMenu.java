package ifs;

import ifs.volumetric.pdf3D;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;

public class ifsMenu extends Component implements ItemListener, ChangeListener, ActionListener, MouseMotionListener {

    ifsys myIfsSys;

    JLabel ptLabel;
    JSpinner xSpinner;
    JSpinner ySpinner;
    JSpinner zSpinner;

    JSpinner brightnessSpinner;
    JSpinner samplesSpinner;
    JSpinner iterationsSpinner;

    JSpinner potentialSpinner;
    JSpinner delaySpinner;
    JSpinner dotSizeSpinner;

    JSpinner xMinSpinner;
    JSpinner xMaxSpinner;
    JSpinner yMinSpinner;
    JSpinner yMaxSpinner;
    JSpinner zMinSpinner;
    JSpinner zMaxSpinner;

    JSpinner pitchSpinner;
    JSpinner yawSpinner;
    JSpinner rollSpinner;

    JSlider camPitchSpinner;
    JSlider camYawSpinner;
    JSlider camRollSpinner;
    JSlider camScaleSpinner;

    JSpinner scaleSpinner;

    JCheckBox frameHoldCheck;

    JCheckBox gridCheck;

    JCheckBox perspectiveCheck;
    JCheckBox delayCheck;
    JCheckBox smearCheck;

    JComboBox renderModeCombo;
    JComboBox pdfModeCombo;

    JPanel pointProperties = new JPanel();
    JPanel renderProperties = new JPanel();
    JPanel pdfProperties = new JPanel();
    JPanel cameraProperties = new JPanel();

    Frame parentFrame = new Frame();

    int pdfXImgFile = 0;
    int pdfYImgFile = 0;
    int pdfZImgFile = 0;

    final JFileChooser fc = new JFileChooser();

    boolean inited=false;
    boolean autoChange = false;
    long lastUiChange = 0;

    long lastPdfPropertiesMouseMoved=0;

    final ChangeListener updateNoClear = new ChangeListener() {
        @Override
        public void stateChanged(ChangeEvent e) {
            if(!autoChange){
                lastUiChange=System.currentTimeMillis();
                myIfsSys.theShape.selectedPt.x = (float)Double.parseDouble(xSpinner.getValue().toString());
                myIfsSys.theShape.selectedPt.y = (float)Double.parseDouble(ySpinner.getValue().toString());
                myIfsSys.theShape.selectedPt.z = (float)Double.parseDouble(zSpinner.getValue().toString());
                myIfsSys.theShape.selectedPt.scale = (float)(0.01 * Double.parseDouble(scaleSpinner.getValue().toString()));

                myIfsSys.theShape.selectedPt.rotationPitch = (float)(Double.parseDouble(pitchSpinner.getValue().toString())/180.0*Math.PI);
                myIfsSys.theShape.selectedPt.rotationYaw = (float)(Double.parseDouble(yawSpinner.getValue().toString())/180.0*Math.PI);
                myIfsSys.theShape.selectedPt.rotationRoll = (float)(Double.parseDouble(rollSpinner.getValue().toString())/180.0*Math.PI);

                myIfsSys.rp.iterations = Integer.parseInt(iterationsSpinner.getValue().toString());
                myIfsSys.rp.dotSize = Integer.parseInt(dotSizeSpinner.getValue().toString());
                myIfsSys.rp.brightnessMultiplier = Double.parseDouble(brightnessSpinner.getValue().toString());
                myIfsSys.rp.samplesPerFrame = Double.parseDouble(samplesSpinner.getValue().toString());

                myIfsSys.theVolume.usePerspective = !perspectiveCheck.isSelected();
                myIfsSys.rp.smearPDF = smearCheck.isSelected();
                myIfsSys.rp.postProcessPeriod = Long.parseLong(delaySpinner.getValue().toString());
                myIfsSys.rp.renderThrottling = delayCheck.isSelected();

                myIfsSys.rp.holdFrame = frameHoldCheck.isSelected();

                myIfsSys.rp.potentialRadius = Integer.parseInt(potentialSpinner.getValue().toString());
                myIfsSys.rp.drawGrid = gridCheck.isSelected();

                myIfsSys.theShape.updateCenter();

                myIfsSys.theVolume.camPitch = camPitchSpinner.getValue() - 180;
                myIfsSys.theVolume.camYaw = camYawSpinner.getValue() - 180;
                myIfsSys.theVolume.camRoll = camRollSpinner.getValue() - 180;
                myIfsSys.theVolume.perspectiveScale = camScaleSpinner.getValue();
                myIfsSys.theVolume.perspectiveScale = Math.max(0.1f, myIfsSys.theVolume.perspectiveScale);

                myIfsSys.lastMoveTime = System.currentTimeMillis();

                myIfsSys.rp.xMin = Integer.parseInt(xMinSpinner.getValue().toString());
                myIfsSys.rp.xMax = Integer.parseInt(xMaxSpinner.getValue().toString());
                myIfsSys.rp.yMin = Integer.parseInt(yMinSpinner.getValue().toString());
                myIfsSys.rp.yMax = Integer.parseInt(yMaxSpinner.getValue().toString());
                myIfsSys.rp.zMin = Integer.parseInt(zMinSpinner.getValue().toString());
                myIfsSys.rp.zMax = Integer.parseInt(zMaxSpinner.getValue().toString());
            }
        }
    };

    ChangeListener updateAndClear = new ChangeListener() {
        @Override
        public void stateChanged(ChangeEvent e) {
            updateNoClear.stateChanged(e);
            myIfsSys.clearframe();
        }
    };

    public JButton addLabeledButton(JButton theButton, SpringLayout layout, JPanel panel, double row, int col){
        int width = 51;
        int totalCols = 3;
        int spinnerLeft = 5 + col*width;
        int spinnerRight = 5 + col*width+ width;
        int labelToSpinner = -5;
        int vspace = 20;
        int topPad=5;

        layout.putConstraint(SpringLayout.WEST, theButton, spinnerLeft, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.EAST, theButton, spinnerRight, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.NORTH, theButton, (int)(topPad+vspace*row), SpringLayout.NORTH, panel);

        //button.addActionListener(this);

        panel.add(theButton);

        return theButton;
    }


    public <T extends JComponent> JComponent addLabeled(T comp, SpringLayout layout, String labelText, JPanel panel, double row){
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

        panel.add(label);
        panel.add(comp);

        return comp;
    }

    public void setupPointPropertiesPanel(JPanel panel){
        xSpinner = new JSpinner();
        ySpinner = new JSpinner();
        zSpinner = new JSpinner();
        scaleSpinner = new JSpinner();

        pitchSpinner = new JSpinner();
        yawSpinner = new JSpinner();
        rollSpinner = new JSpinner();

        ptLabel = new JLabel("Point -1");

        int topPad=5;

        SpringLayout layout = new SpringLayout();
        panel.setLayout(layout);

        layout.putConstraint(SpringLayout.NORTH, ptLabel, topPad, SpringLayout.NORTH, panel);

        ((JSpinner)addLabeled(xSpinner, layout, "X", panel, 1)).addChangeListener(updateAndClear);
        ((JSpinner)addLabeled(ySpinner, layout, "Y", panel, 2)).addChangeListener(updateAndClear);
        ((JSpinner)addLabeled(zSpinner, layout, "Z", panel, 3)).addChangeListener(updateAndClear);
        ((JSpinner)addLabeled(scaleSpinner, layout, "Scale %", panel, 4)).addChangeListener(updateAndClear);
        ((JSpinner)addLabeled(pitchSpinner, layout, "Pitch°", panel, 6)).addChangeListener(updateAndClear);
        ((JSpinner)addLabeled(yawSpinner, layout, "Yaw°", panel, 7)).addChangeListener(updateAndClear);
        ((JSpinner)addLabeled(rollSpinner, layout, "Roll°", panel, 8)).addChangeListener(updateAndClear);
        panel.add(ptLabel);
    }

    public void setupPdfPropertiesPanel(JPanel panel){
        //JLabel renderLabel = new JLabel(" PDF Properties");

        smearCheck = new JCheckBox();

        int topPad=5;

        SpringLayout layout = new SpringLayout();
        panel.setLayout(layout);
        layout.putConstraint(SpringLayout.NORTH, ptLabel, topPad, SpringLayout.NORTH, panel);

        final Component parent = this;

        addLabeledButton(new JButton("X"), layout, panel, 0, 0).addActionListener(this);
        addLabeledButton(new JButton("Y"), layout, panel, 0, 1).addActionListener(this);
        addLabeledButton(new JButton("Z"), layout, panel, 0, 2).addActionListener(this);
        ((JComboBox)addLabeled(pdfModeCombo, layout, "Mix", panel, 2)).addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox)e.getSource();
                if(cb.getSelectedItem() == pdf3D.comboMode.ADD.toString()){
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
            }
        });

        ((JCheckBox)addLabeled(smearCheck, layout, "Smear PDF", panel, 3.5)).addChangeListener(updateAndClear);

        panel.addMouseMotionListener(this);
    }

    public void setupCameraPropertiesPanel(JPanel panel){
        camPitchSpinner = new JSlider();
        camYawSpinner = new JSlider();
        camRollSpinner = new JSlider();
        camScaleSpinner = new JSlider();

        camPitchSpinner.setMaximum(360);
        camYawSpinner.setMaximum(360);
        camRollSpinner.setMaximum(360);
        camScaleSpinner.setMaximum(500);

        SpringLayout layout = new SpringLayout();
        panel.setLayout(layout);



        ((JSlider)addLabeled(camPitchSpinner, layout, "Pitch", panel, 1+1)).addChangeListener(updateAndClear);
        ((JSlider)addLabeled(camYawSpinner, layout, "Yaw", panel, 2.35+1)).addChangeListener(updateAndClear);
        ((JSlider)addLabeled(camRollSpinner, layout, "Roll", panel, 3.7+1)).addChangeListener(updateAndClear);
        ((JSlider)addLabeled(camScaleSpinner, layout, "FOV", panel, 5+1)).addChangeListener(updateAndClear);

        ActionListener moveCamera = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JButton cb = (JButton)e.getSource();

                if(cb.getText()=="YZ"){
                    myIfsSys.theVolume.camPitch=-90;
                    myIfsSys.theVolume.camRoll=-90;
                    myIfsSys.theVolume.camYaw=0;
                }else if(cb.getText()=="XY"){
                    //TOP VIEW
                    myIfsSys.theVolume.camPitch=0;
                    myIfsSys.theVolume.camRoll=0;
                    myIfsSys.theVolume.camYaw=0;
                }else if(cb.getText()=="XZ"){
                    //SIDE VIEW
                    myIfsSys.theVolume.camPitch=0;
                    myIfsSys.theVolume.camRoll=-90;
                    myIfsSys.theVolume.camYaw=0;
                }
                myIfsSys.clearframe();
            }
        };

        addLabeledButton(new JButton("XY"), layout, panel, 0, 0).addActionListener(moveCamera);
        addLabeledButton(new JButton("YZ"), layout, panel, 0, 1).addActionListener(moveCamera);
        addLabeledButton(new JButton("XZ"), layout, panel, 0, 2).addActionListener(moveCamera);

        perspectiveCheck = new JCheckBox();
        ((JCheckBox)addLabeled(perspectiveCheck, layout, "Ortho", panel, 8)).addChangeListener(updateAndClear);
    }

    public void setupRenderPropertiesPanel(JPanel panel){
        brightnessSpinner = new JSpinner();
        samplesSpinner = new JSpinner();
        iterationsSpinner = new JSpinner();

        potentialSpinner = new JSpinner();
        delaySpinner = new JSpinner();
        dotSizeSpinner = new JSpinner();

        frameHoldCheck = new JCheckBox();

        gridCheck = new JCheckBox();

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

        SpringLayout layout = new SpringLayout();
        panel.setLayout(layout);

        ((JComboBox)addLabeled(renderModeCombo, layout, "Mode", panel, 0.7)).addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox)e.getSource();
                if(cb.getSelectedItem() == volume.RenderMode.VOLUMETRIC.toString()){
                    myIfsSys.theVolume.renderMode = volume.RenderMode.VOLUMETRIC;
                }else if(cb.getSelectedItem() == volume.RenderMode.PROJECT_ONLY.toString()){
                    myIfsSys.theVolume.renderMode = volume.RenderMode.PROJECT_ONLY;
                }
            }
        });

        ((JSpinner)addLabeled(brightnessSpinner, layout, "Brightness", panel, 2)).addChangeListener(updateNoClear);
        ((JSpinner)addLabeled(samplesSpinner, layout, "Dots/Frame", panel, 3)).addChangeListener(updateNoClear);
        ((JSpinner)addLabeled(iterationsSpinner, layout, "Iterations", panel, 4)).addChangeListener(updateAndClear);

        ((JCheckBox)addLabeled(frameHoldCheck, layout, "Hold Frame", panel, 5.5)).addChangeListener(updateNoClear);

        ((JSpinner)addLabeled(potentialSpinner, layout, "Blur", panel, 6.6)).addChangeListener(updateAndClear);
        ((JCheckBox)addLabeled(gridCheck, layout, "Grid", panel, 7.6)).addChangeListener(updateAndClear);

        ((JCheckBox)addLabeled(delayCheck, layout, "Framelimit", panel, 12.5-3)).addChangeListener(updateNoClear);
        ((JSpinner)addLabeled(delaySpinner, layout, "Wait X ms", panel, 13.6-3)).addChangeListener(updateNoClear);
        ((JSpinner)addLabeled(dotSizeSpinner, layout, "Dot Size", panel, 14.7-3)).addChangeListener(updateNoClear);

        xMaxSpinner=new JSpinner();
        xMinSpinner=new JSpinner();
        yMaxSpinner=new JSpinner();
        yMinSpinner=new JSpinner();
        zMaxSpinner=new JSpinner();
        zMinSpinner=new JSpinner();

        ((JSpinner)addLabeled(xMinSpinner, layout, "Xmin", panel, 13.6)).addChangeListener(updateAndClear);
        ((JSpinner)addLabeled(xMaxSpinner, layout, "Xmax", panel, 14.6)).addChangeListener(updateAndClear);
        ((JSpinner)addLabeled(yMinSpinner, layout, "Ymin", panel, 15.6)).addChangeListener(updateAndClear);
        ((JSpinner)addLabeled(yMaxSpinner, layout, "Ymax", panel, 16.6)).addChangeListener(updateAndClear);
        ((JSpinner)addLabeled(zMinSpinner, layout, "Zmin", panel, 17.6)).addChangeListener(updateAndClear);
        ((JSpinner)addLabeled(zMaxSpinner, layout, "Zmax", panel, 18.6)).addChangeListener(updateAndClear);
    }

    public ifsMenu(Frame f, ifsys is){
        is.addMouseListener(new PopClickListener());
        inited=false;
        parentFrame=f;
        myIfsSys = is;
        pointProperties = new JPanel();
        renderProperties = new JPanel();
        pdfProperties = new JPanel();
        cameraProperties = new JPanel();

        //SIDE MENU

        setupPointPropertiesPanel(pointProperties);
        setupRenderPropertiesPanel(renderProperties);
        setupPdfPropertiesPanel(pdfProperties);
        setupCameraPropertiesPanel(cameraProperties);

        SpringLayout sideMenuLayout = new SpringLayout();
        //sideMenu.setLayout(sideMenuLayout);

        JSplitPane splitPaneBottom = new JSplitPane(JSplitPane.VERTICAL_SPLIT, cameraProperties, pdfProperties);
        JSplitPane splitPaneTop = new JSplitPane(JSplitPane.VERTICAL_SPLIT, pointProperties, renderProperties);

        JSplitPane splitPaneBig = new JSplitPane(JSplitPane.VERTICAL_SPLIT, splitPaneTop, splitPaneBottom);


        splitPaneTop.setOrientation(JSplitPane.VERTICAL_SPLIT);
        splitPaneTop.setDividerLocation(200);

        splitPaneBottom.setOrientation(JSplitPane.VERTICAL_SPLIT);
        splitPaneBottom.setDividerLocation(170);

        splitPaneBig.setDividerLocation(640);

        MenuBar menuBar;
        Menu fileMenu, renderMenu, shapeMenu, guidesMenu, viewMenu;

        menuBar = new MenuBar();
        fileMenu = new Menu("File");
        renderMenu = new Menu("Render");
        shapeMenu = new Menu("Shape");
        guidesMenu = new Menu("Guides");

        //FILE MENU
            MenuItem saveButton = new MenuItem("Save Shape...");
            saveButton.addActionListener(this);
            fileMenu.add(saveButton);

            MenuItem loadButton = new MenuItem("Load Shape...");
            loadButton.addActionListener(this);
            fileMenu.add(loadButton);

        //RENDER MENU
            CheckboxMenuItem aaButton = new CheckboxMenuItem("Anti-Aliasing"); //anti-aliasing toggle
            aaButton.setState(is.theVolume.antiAliasing);
            aaButton.addItemListener(this);
            renderMenu.add(aaButton);

        //SHAPE MENU
            CheckboxMenuItem autoScaleButton = new CheckboxMenuItem("AutoScale Points"); //autoscale toggle
            autoScaleButton.setState(is.theShape.autoScale);
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

            //menuBar.add(renderMenu);
            //menuBar.add(shapeMenu);
            //menuBar.add(guidesMenu);
            menuBar.add(fileMenu);

            f.setMenuBar(menuBar);
            inited=true;
    }

    public void updateSideMenu(){
        myIfsSys.rp.limitParams();
        autoChange = true;
        ptLabel.setText(" Point " + myIfsSys.theShape.pointSelected + " Properties:");
        if(inited && System.currentTimeMillis()-lastUiChange>100){
            if(myIfsSys.theShape.pointSelected !=-1){
                xSpinner.setValue(myIfsSys.theShape.selectedPt.x);
                ySpinner.setValue(myIfsSys.theShape.selectedPt.y);
                zSpinner.setValue(myIfsSys.theShape.selectedPt.z);

                camPitchSpinner.setValue((int)myIfsSys.theVolume.camPitch + 180);
                camRollSpinner.setValue((int)myIfsSys.theVolume.camRoll + 180);
                camYawSpinner.setValue((int)myIfsSys.theVolume.camYaw + 180);
                camScaleSpinner.setValue((int)myIfsSys.theVolume.perspectiveScale);

                scaleSpinner.setValue(myIfsSys.theShape.selectedPt.scale * 100);

                pitchSpinner.setValue(myIfsSys.theShape.selectedPt.rotationPitch/Math.PI*180);
                yawSpinner.setValue(myIfsSys.theShape.selectedPt.rotationYaw/Math.PI*180);
                rollSpinner.setValue(myIfsSys.theShape.selectedPt.rotationRoll/Math.PI*180);

                brightnessSpinner.setValue(myIfsSys.rp.brightnessMultiplier);
                samplesSpinner.setValue(myIfsSys.rp.samplesPerFrame);
                iterationsSpinner.setValue(myIfsSys.rp.iterations);
                potentialSpinner.setValue(myIfsSys.rp.potentialRadius);
                delaySpinner.setValue(myIfsSys.rp.postProcessPeriod);

                perspectiveCheck.setSelected(!myIfsSys.theVolume.usePerspective);
                frameHoldCheck.setSelected(myIfsSys.rp.holdFrame);
                gridCheck.setSelected(myIfsSys.rp.drawGrid);
                delayCheck.setSelected(myIfsSys.rp.renderThrottling);
                //System.out.println(renderModeCombo.setse);
                renderModeCombo.setSelectedIndex(myIfsSys.theVolume.renderMode == volume.RenderMode.PROJECT_ONLY ? 1 : 0);
                dotSizeSpinner.setValue(myIfsSys.rp.dotSize);

                xMinSpinner.setValue(myIfsSys.rp.xMin);
                xMaxSpinner.setValue(myIfsSys.rp.xMax);
                yMinSpinner.setValue(myIfsSys.rp.yMin);
                yMaxSpinner.setValue(myIfsSys.rp.yMax);
                zMinSpinner.setValue(myIfsSys.rp.zMin);
                zMaxSpinner.setValue(myIfsSys.rp.zMax);
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
                myIfsSys.theShape.autoScale = e.getStateChange()==1;
            }
            if(e.getItem()=="PDF Samples"){
                myIfsSys.rp.usePDFSamples = e.getStateChange()==1;
            }
    }

    @Override
    public void stateChanged(ChangeEvent e) {

    }

    public void actionPerformed(ActionEvent e) {
        JButton cb = new JButton();// = (JButton)e.getSource();
        MenuItem mc = new MenuItem();// = (MenuItem)e.getSource();

        try{
            cb = (JButton)e.getSource();
        }catch (Exception _e){

        }

        try{
            mc = (MenuItem)e.getSource();
            System.out.println(mc.getActionCommand());
        }catch (Exception _e){

        }

        if(cb.getText()=="X"){
            pdfXImgFile = fc.showOpenDialog(this);
            if(pdfXImgFile == JFileChooser.APPROVE_OPTION){
                myIfsSys.thePdf.setSampleImage(fc.getSelectedFile(), pdf3D.Dimension.X);
            }
        }else if(cb.getText()=="Y"){
            pdfYImgFile = fc.showOpenDialog(this);
            if(pdfYImgFile == JFileChooser.APPROVE_OPTION){
                myIfsSys.thePdf.setSampleImage(fc.getSelectedFile(), pdf3D.Dimension.Y);
            }
        }else if(cb.getText()=="Z"){
            pdfZImgFile = fc.showOpenDialog(this);
            if(pdfZImgFile == JFileChooser.APPROVE_OPTION){
                myIfsSys.thePdf.setSampleImage(fc.getSelectedFile(), pdf3D.Dimension.Z);
            }
        }else if(mc.getActionCommand()=="Save Shape..."){
            pdfZImgFile = fc.showSaveDialog(this);
            if(pdfZImgFile == JFileChooser.APPROVE_OPTION){
                System.out.println("saving " + fc.getSelectedFile().getName());
                myIfsSys.saveStuff(fc.getSelectedFile().getName());
            }
        }else if(mc.getActionCommand()=="Load Shape..."){
            pdfZImgFile = fc.showOpenDialog(this);
            if(pdfZImgFile == JFileChooser.APPROVE_OPTION){
                System.out.println("loading " + fc.getSelectedFile().getName());
                myIfsSys.loadStuff(fc.getSelectedFile().getName());
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

    class PopClickListener extends MouseAdapter {
        public void mousePressed(MouseEvent e){
            if (SwingUtilities.isRightMouseButton (e))
                doPop(e);
        }

        private void doPop(MouseEvent e){
            myPopUpMenu menu = new myPopUpMenu();
            menu.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    class myPopUpMenu extends JPopupMenu {
        JMenuItem anItem;
        public myPopUpMenu(){
            anItem = new JMenuItem("Menu1");
            add(anItem);
        }
    }
}
