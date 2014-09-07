package ifs;

import com.alee.extended.colorchooser.WebGradientColorChooser;
import com.alee.extended.panel.WebButtonGroup;
import com.alee.laf.button.WebButton;
import com.alee.laf.scroll.WebScrollPane;
import com.alee.laf.table.WebTable;
import ifs.thirdparty.ImagePreviewPanel;
import ifs.volumetric.pdf3D;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;

public class ifsMenu extends Component implements ItemListener, ChangeListener, ActionListener, MouseMotionListener {

    ifsys myIfsSys;

    JLabel ptLabel;
    JLabel generationLabel;
    JSpinner xSpinner;
    JSpinner ySpinner;
    JSpinner zSpinner;

    JSpinner brightnessSpinner;
    JSpinner samplesSpinner;
    JSpinner iterationsSpinner;

    JSpinner potentialSpinner;
    JSpinner delaySpinner;
    //JSpinner dotSizeSpinner;

    //JSpinner xMinSpinner;
    //JSpinner xMaxSpinner;
    //JSpinner yMinSpinner;
    //JSpinner yMaxSpinner;
    //JSpinner zMinSpinner;
    //JSpinner zMaxSpinner;

    JSpinner pitchSpinner;
    JSpinner yawSpinner;
    JSpinner rollSpinner;

    //JSlider camPitchSpinner;
    //JSlider camYawSpinner;
    //JSlider camRollSpinner;
    JSlider camScaleSpinner;

    JSpinner scaleSpinner;

    JCheckBox frameHoldCheck;

    JCheckBox gridCheck;
    JCheckBox cartoonCheck;

    JCheckBox perspectiveCheck;
    JCheckBox delayCheck;
    JCheckBox smearCheck;

    JComboBox renderModeCombo;
    JComboBox evolveModeCombo;
    JComboBox pdfModeCombo;

    WebTable evolutionTable;

    WebGradientColorChooser colorChooser;

    JPanel pointProperties = new JPanel();
    JPanel renderProperties = new JPanel();
    JPanel pdfProperties = new JPanel();
    //JPanel cameraProperties = new JPanel();
    JPanel evolveProperties = new JPanel();

    Frame parentFrame = new Frame();

    int pdfXImgFile = 0;
    int pdfYImgFile = 0;
    int pdfZImgFile = 0;

    JFileChooser fc;

    boolean inited=false;
    boolean autoChange = false;
    long lastUiChange = 0;

    long lastPdfPropertiesMouseMoved=0;

    final ChangeListener updateNoClear = new ChangeListener() {
        @Override
        public void stateChanged(ChangeEvent e) {
            if(!autoChange){

                ifsPt editPt = new ifsPt();
                if(myIfsSys.evolutionDescSelected){
                    editPt = myIfsSys.eShape.mutationDescriptorPt;
                }else{
                    editPt = myIfsSys.theShape.selectedPt;
                }

                lastUiChange=System.currentTimeMillis();
                editPt.x = (float)Double.parseDouble(xSpinner.getValue().toString());
                editPt.y = (float)Double.parseDouble(ySpinner.getValue().toString());
                editPt.z = (float)Double.parseDouble(zSpinner.getValue().toString());
                editPt.scale = (float)(0.01 * Double.parseDouble(scaleSpinner.getValue().toString()));

                editPt.rotationPitch = (float)(Double.parseDouble(pitchSpinner.getValue().toString())/180.0*Math.PI);
                editPt.rotationYaw = (float)(Double.parseDouble(yawSpinner.getValue().toString())/180.0*Math.PI);
                editPt.rotationRoll = (float)(Double.parseDouble(rollSpinner.getValue().toString())/180.0*Math.PI);

                myIfsSys.rp.iterations = Integer.parseInt(iterationsSpinner.getValue().toString());
                //myIfsSys.rp.dotSize = Integer.parseInt(dotSizeSpinner.getValue().toString());
                myIfsSys.rp.brightnessMultiplier = Double.parseDouble(brightnessSpinner.getValue().toString());
                myIfsSys.rp.samplesPerFrame = (int)Double.parseDouble(samplesSpinner.getValue().toString());

                myIfsSys.theVolume.usePerspective = !perspectiveCheck.isSelected();
                myIfsSys.rp.smearPDF = smearCheck.isSelected();
                myIfsSys.rp.postProcessPeriod = Long.parseLong(delaySpinner.getValue().toString());
                myIfsSys.rp.renderThrottling = delayCheck.isSelected();

                myIfsSys.rp.holdFrame = frameHoldCheck.isSelected();

                myIfsSys.rp.potentialRadius = Integer.parseInt(potentialSpinner.getValue().toString());
                myIfsSys.rp.drawGrid = gridCheck.isSelected();
                myIfsSys.rp.useShadows = cartoonCheck.isSelected();

                myIfsSys.theShape.updateCenter();

                //myIfsSys.theVolume.camPitch = camPitchSpinner.getValue() - 180;
                //myIfsSys.theVolume.camYaw = camYawSpinner.getValue() - 180;
                //myIfsSys.theVolume.camRoll = camRollSpinner.getValue() - 180;
                myIfsSys.theVolume.perspectiveScale = camScaleSpinner.getValue();
                myIfsSys.theVolume.perspectiveScale = Math.max(0.1f, myIfsSys.theVolume.perspectiveScale);

                myIfsSys.lastMoveTime = System.currentTimeMillis();

                //myIfsSys.rp.xMin = Integer.parseInt(xMinSpinner.getValue().toString());
                //myIfsSys.rp.xMax = Integer.parseInt(xMaxSpinner.getValue().toString());
                //myIfsSys.rp.yMin = Integer.parseInt(yMinSpinner.getValue().toString());
                //myIfsSys.rp.yMax = Integer.parseInt(yMaxSpinner.getValue().toString());
                //myIfsSys.rp.zMin = Integer.parseInt(zMinSpinner.getValue().toString());
                //myIfsSys.rp.zMax = Integer.parseInt(zMaxSpinner.getValue().toString());
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

    public JButton addLabeledButton(WebButton theButton, SpringLayout layout, JPanel panel, int col){
        int width = 51;

        int spinnerLeft = 5 + col*width;
        int spinnerRight = 5 + col*width+ width;

        int topPad=25;

        layout.putConstraint(SpringLayout.WEST, theButton, spinnerLeft, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.EAST, theButton, spinnerRight, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.NORTH, theButton, (int)(topPad+panel.getHeight()), SpringLayout.NORTH, panel);

        //button.addActionListener(this);

        panel.add(theButton);

        return theButton;
    }
    public <T extends JComponent> JComponent addLabeled(T comp, SpringLayout layout, String labelText, JPanel panel){
        return addLabeled(comp, layout, labelText, panel, false);
    }

    public <T extends JComponent> JComponent addLabeled(T comp, SpringLayout layout, String labelText, JPanel panel, boolean noLabel){
        int compLeft = 70;
        int compRight = -5;
        int compToSpinner = -5;
        int topPad=25;

        if(noLabel){ //things without labels get centered...
            compLeft=30;
        }

        JLabel label = new JLabel(labelText);

        layout.putConstraint(SpringLayout.EAST, label, compToSpinner, SpringLayout.WEST, comp);
        layout.putConstraint(SpringLayout.WEST, comp, compLeft, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.EAST, comp, compRight, SpringLayout.EAST, panel);
        layout.putConstraint(SpringLayout.NORTH, comp, (int)(panel.getHeight()+0), SpringLayout.NORTH, panel);
        layout.putConstraint(SpringLayout.NORTH, label, (int) (panel.getHeight() + 0), SpringLayout.NORTH, panel);

        if(!noLabel)panel.add(label);
        panel.add(comp);panel.setSize(200,topPad+panel.getHeight());

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
        //panel.add(ptLabel);
        //addLabel(ptLabel, layout, panel);
        ((JLabel)addLabeled(ptLabel, layout, "Point", panel)).addComponentListener(null);
        ((JSpinner)addLabeled(xSpinner, layout, "X", panel)).addChangeListener(updateAndClear);
        ((JSpinner)addLabeled(ySpinner, layout, "Y", panel)).addChangeListener(updateAndClear);
        ((JSpinner)addLabeled(zSpinner, layout, "Z", panel)).addChangeListener(updateAndClear);
        ((JSpinner)addLabeled(scaleSpinner, layout, "Scale %", panel)).addChangeListener(updateAndClear);
        ((JSpinner)addLabeled(pitchSpinner, layout, "Pitch°", panel)).addChangeListener(updateAndClear);
        ((JSpinner)addLabeled(yawSpinner, layout, "Yaw°", panel)).addChangeListener(updateAndClear);
        ((JSpinner)addLabeled(rollSpinner, layout, "Roll°", panel)).addChangeListener(updateAndClear);

    }

    public void setupPdfPropertiesPanel(JPanel panel){
        //JLabel renderLabel = new JLabel(" PDF Properties");

        smearCheck = new JCheckBox();

        int topPad=5;

        SpringLayout layout = new SpringLayout();
        panel.setLayout(layout);
        layout.putConstraint(SpringLayout.NORTH, ptLabel, topPad, SpringLayout.NORTH, panel);

        final Component parent = this;
/*
        ((JComboBox)addLabeled(pdfModeCombo, layout, "Mix", panel)).addActionListener(new ActionListener() {
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
*/
        ((JCheckBox)addLabeled(smearCheck, layout, "Smear PDF", panel)).addChangeListener(updateAndClear);

        WebButton XButton = new WebButton("", new ImageIcon("./instant-ifs/icons/front.png"));
        WebButton ZButton = new WebButton("", new ImageIcon("./instant-ifs/icons/side.png"));
        WebButton YButton = new WebButton("", new ImageIcon("./instant-ifs/icons/top.png"));

        XButton.setName("X");XButton.addActionListener(this);
        YButton.setName("Y");YButton.addActionListener(this);
        ZButton.setName("Z");ZButton.addActionListener(this);

        WebButtonGroup iconsGroup = new WebButtonGroup ( true, XButton, YButton, ZButton );
        ((WebButtonGroup)addLabeled(iconsGroup, layout, "", panel, true)).addComponentListener(null);

        panel.addMouseMotionListener(this);
    }


    public void setupEvolvePropertiesPanel(JPanel panel){

        int topPad=5;
        SpringLayout layout = new SpringLayout();
        panel.setLayout(layout);
        layout.putConstraint(SpringLayout.NORTH, ptLabel, topPad, SpringLayout.NORTH, panel);

        generationLabel = new JLabel("0 Sibling 0/"+myIfsSys.eShape.sibsPerGen);

        ((JLabel)addLabeled(generationLabel, layout, "Generation", panel)).addComponentListener(null);

        WebButton ParentsButton = new WebButton("", new ImageIcon("./instant-ifs/icons/parents.png"));
        WebButton OffspingButton = new WebButton("", new ImageIcon("./instant-ifs/icons/offspring.png"));

        WebButton NextSibButton = new WebButton("", new ImageIcon("./instant-ifs/icons/next.png"));
        WebButton PrevSibButton = new WebButton("", new ImageIcon("./instant-ifs/icons/back.png"));

        ParentsButton.setName("parents");ParentsButton.addActionListener(this);
        OffspingButton.setName("offspring");OffspingButton.addActionListener(this);
        NextSibButton.setName("nextsib");NextSibButton.addActionListener(this);
        PrevSibButton.setName("prevsib");PrevSibButton.addActionListener(this);

        WebButtonGroup iconsGroup = new WebButtonGroup ( true, OffspingButton, ParentsButton, PrevSibButton, NextSibButton );
        ((WebButtonGroup)addLabeled(iconsGroup, layout, "", panel, true)).addComponentListener(null);

        String[] modeStrings = {
                ScoreParams.Presets.MAX_SURFACE.toString(),
                ScoreParams.Presets.MAX_SurfaceVolume.toString(),
                ScoreParams.Presets.MAX_TRAVEL.toString(),
                ScoreParams.Presets.MAX_VOLUME.toString(),
                ScoreParams.Presets.MIN_DistSurface.toString(),
                ScoreParams.Presets.MIN_DistVolume.toString(),
                "_CUSTOM"
        };

        evolveModeCombo = new JComboBox(modeStrings);
        ((JLabel)addLabeled(new JLabel(), layout, "", panel, true)).addComponentListener(null);
        ((JComboBox)addLabeled(evolveModeCombo, layout, "Score By", panel)).addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox) e.getSource();
                if (cb.getSelectedItem() == ScoreParams.Presets.MAX_SURFACE.toString()) {
                    myIfsSys.rp.scoreParams = new ScoreParams(ScoreParams.Presets.MAX_SURFACE);
                } else if (cb.getSelectedItem() == ScoreParams.Presets.MAX_SurfaceVolume.toString()) {
                    myIfsSys.rp.scoreParams = new ScoreParams(ScoreParams.Presets.MAX_SurfaceVolume);
                }else if (cb.getSelectedItem() == ScoreParams.Presets.MAX_TRAVEL.toString()) {
                    myIfsSys.rp.scoreParams = new ScoreParams(ScoreParams.Presets.MAX_TRAVEL);
                }else if (cb.getSelectedItem() == ScoreParams.Presets.MAX_VOLUME.toString()) {
                    myIfsSys.rp.scoreParams = new ScoreParams(ScoreParams.Presets.MAX_VOLUME);
                }else if (cb.getSelectedItem() == ScoreParams.Presets.MIN_DistSurface.toString()) {
                    myIfsSys.rp.scoreParams = new ScoreParams(ScoreParams.Presets.MIN_DistSurface);
                }else if (cb.getSelectedItem() == ScoreParams.Presets.MIN_DistVolume.toString()) {
                    myIfsSys.rp.scoreParams = new ScoreParams(ScoreParams.Presets.MIN_DistVolume);
                }

                myIfsSys.eShape.myScoreParams = myIfsSys.rp.scoreParams;
            }
        });

        ((JLabel)addLabeled(new JLabel(), layout, "", panel)).addComponentListener(null);
        updateEvolutionTable();

        //evolutionTable.setEditable(false);
        //evolutionTable.setAutoResizeMode(WebTable.AUTO_RESIZE_OFF);
        //evolutionTable.setRowSelectionAllowed(true);
        //evolutionTable.setColumnSelectionAllowed(true);
        evolutionTable.setPreferredScrollableViewportSize(new Dimension(200, 150));
        evolutionTable.setAutoCreateRowSorter(true);
        evolutionTable.setAutoscrolls(true);
         ((WebScrollPane)addLabeled(new WebScrollPane ( evolutionTable ), layout, "", panel, true)).addComponentListener(new ComponentListener() {
             @Override
             public void componentResized(ComponentEvent e) {

             }

             @Override
             public void componentMoved(ComponentEvent e) {

             }

             @Override
             public void componentShown(ComponentEvent e) {

             }

             @Override
             public void componentHidden(ComponentEvent e) {

             }
         });

        panel.addMouseMotionListener(this);
    }

    public void updateEvolutionTable(){
        String[] headers = { "Date", "Name", "Score", "Q", "Method"};
        //System.out.println("HISTORY"+myIfsSys.eShape.historyIndex);
        evolutionTable = new WebTable (myIfsSys.eShape.evolutionHistory, headers );
        //evolutionTable.update(myIfsSys.renderGraphics);
        evolutionTable.scrollToRow(myIfsSys.eShape.historyIndex);
        evolveProperties.requestFocusInWindow();
        myIfsSys.requestFocusInWindow();
        myIfsSys.clearframe();
        myIfsSys.gamefunc();
    }

    public void setupRenderPropertiesPanel(JPanel panel){
        brightnessSpinner = new JSpinner();
        samplesSpinner = new JSpinner();
        iterationsSpinner = new JSpinner();

        potentialSpinner = new JSpinner();
        delaySpinner = new JSpinner();
        //dotSizeSpinner = new JSpinner();

        frameHoldCheck = new JCheckBox();

        gridCheck = new JCheckBox();
        cartoonCheck = new JCheckBox();

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

        ((JComboBox)addLabeled(renderModeCombo, layout, "Mode", panel)).addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox) e.getSource();
                if (cb.getSelectedItem() == volume.RenderMode.VOLUMETRIC.toString()) {
                    myIfsSys.theVolume.renderMode = volume.RenderMode.VOLUMETRIC;
                } else if (cb.getSelectedItem() == volume.RenderMode.PROJECT_ONLY.toString()) {
                    myIfsSys.theVolume.renderMode = volume.RenderMode.PROJECT_ONLY;
                }
            }
        });

        ((JSpinner)addLabeled(brightnessSpinner, layout, "Brightness", panel)).addChangeListener(updateNoClear);
        ((JSpinner)addLabeled(samplesSpinner, layout, "Dots/Frame", panel)).addChangeListener(updateNoClear);
        ((JSpinner)addLabeled(iterationsSpinner, layout, "Iterations", panel)).addChangeListener(updateAndClear);

        //((JSpinner)addLabeled(potentialSpinner, layout, "Blur", panel)).addChangeListener(updateAndClear);
        ((JCheckBox)addLabeled(cartoonCheck, layout, "Cartoon", panel)).addChangeListener(updateAndClear);

        ((JCheckBox)addLabeled(gridCheck, layout, "Grid", panel)).addChangeListener(updateAndClear);
        ((JCheckBox)addLabeled(frameHoldCheck, layout, "Hold Frame", panel)).addChangeListener(updateNoClear);

        ((JCheckBox)addLabeled(delayCheck, layout, "Framelimit", panel)).addChangeListener(updateNoClear);
        ((JSpinner)addLabeled(delaySpinner, layout, "Wait X ms", panel)).addChangeListener(updateNoClear);
        //((JSpinner)addLabeled(dotSizeSpinner, layout, "Dot Size", panel)).addChangeListener(updateNoClear);

        //xMaxSpinner=new JSpinner();
        //xMinSpinner=new JSpinner();
        //yMaxSpinner=new JSpinner();
        //yMinSpinner=new JSpinner();
        //zMaxSpinner=new JSpinner();
        //zMinSpinner=new JSpinner();

        //((JSpinner)addLabeled(xMinSpinner, layout, "Xmin", panel)).addChangeListener(updateAndClear);
        //((JSpinner)addLabeled(xMaxSpinner, layout, "Xmax", panel)).addChangeListener(updateAndClear);
        //((JSpinner)addLabeled(yMinSpinner, layout, "Ymin", panel)).addChangeListener(updateAndClear);
        //((JSpinner)addLabeled(yMaxSpinner, layout, "Ymax", panel)).addChangeListener(updateAndClear);
        //((JSpinner)addLabeled(zMinSpinner, layout, "Zmin", panel)).addChangeListener(updateAndClear);
        //((JSpinner)addLabeled(zMaxSpinner, layout, "Zmax", panel)).addChangeListener(updateAndClear);

        colorChooser = new WebGradientColorChooser();
        //colorChooser.setPreferredWidth(350);
        // ((WebGradientColorChooser)addLabeled(colorChooser, layout, "Color", panel)).addChangeListener(updateAndClear);





        //camPitchSpinner = new JSlider();
        //camYawSpinner = new JSlider();
        //camRollSpinner = new JSlider();
        camScaleSpinner = new JSlider();

        //camPitchSpinner.setMaximum(360);
        //camYawSpinner.setMaximum(360);
        //camRollSpinner.setMaximum(360);
        camScaleSpinner.setMaximum(500);




        //((JSlider)addLabeled(camPitchSpinner, layout, "Pitch", panel)).addChangeListener(updateAndClear);
        //((JSlider)addLabeled(camYawSpinner, layout, "Yaw", panel)).addChangeListener(updateAndClear);
        //((JSlider)addLabeled(camRollSpinner, layout, "Roll", panel)).addChangeListener(updateAndClear);
        ((JSlider)addLabeled(camScaleSpinner, layout, "FOV", panel)).addChangeListener(updateAndClear);

        ActionListener moveCamera = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                WebButton cb = (WebButton)(e.getSource());
                System.out.println("NAME  " + cb.getName());
                if(cb.getName()=="YZ"){
                    myIfsSys.theVolume.camPitch=-90;
                    myIfsSys.theVolume.camRoll=-90;
                    myIfsSys.theVolume.camYaw=0;
                }else if(cb.getName()=="XY"){
                    //TOP VIEW
                    myIfsSys.theVolume.camPitch=0;
                    myIfsSys.theVolume.camRoll=0;
                    myIfsSys.theVolume.camYaw=0;
                }else if(cb.getName()=="XZ"){
                    //SIDE VIEW
                    myIfsSys.theVolume.camPitch=0;
                    myIfsSys.theVolume.camRoll=-90;
                    myIfsSys.theVolume.camYaw=0;
                }
                myIfsSys.clearframe();
            }
        };



        perspectiveCheck = new JCheckBox();
        ((JCheckBox)addLabeled(perspectiveCheck, layout, "Ortho", panel)).addChangeListener(updateAndClear);

        WebButton YZButton = new WebButton("", new ImageIcon("./instant-ifs/icons/front.png"));
        WebButton XYButton = new WebButton("", new ImageIcon("./instant-ifs/icons/side.png"));
        WebButton XZButton = new WebButton("", new ImageIcon("./instant-ifs/icons/top.png"));

        YZButton.setName("YZ");YZButton.addActionListener(moveCamera);
        XYButton.setName("XZ");XYButton.addActionListener(moveCamera);
        XZButton.setName("XY");XZButton.addActionListener(moveCamera);

        WebButtonGroup iconsGroup = new WebButtonGroup ( true, YZButton, XZButton, XYButton );
        ((WebButtonGroup)addLabeled(iconsGroup, layout, "dir", panel, true)).addComponentListener(null);



    }

    public ifsMenu(Frame f, ifsys is){
        is.addMouseListener(new PopClickListener());
        inited=false;
        parentFrame=f;
        myIfsSys = is;
        pointProperties = new JPanel();
        renderProperties = new JPanel();
        pdfProperties = new JPanel();
        //cameraProperties = new JPanel();
        evolveProperties = new JPanel();

        //SIDE MENU

        setupPointPropertiesPanel(pointProperties);
        setupRenderPropertiesPanel(renderProperties);
        setupPdfPropertiesPanel(pdfProperties);
        //setupCameraPropertiesPanel(cameraProperties);
        setupEvolvePropertiesPanel(evolveProperties);

        SpringLayout sideMenuLayout = new SpringLayout();

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

        fc = new JFileChooser("./instant-ifs/img");
        ImagePreviewPanel preview = new ImagePreviewPanel();
        fc.setAccessory(preview);
        fc.addPropertyChangeListener(preview);
    }

    public void updateSideMenu(){
        myIfsSys.rp.limitParams();
        autoChange = true;
        generationLabel.setText(myIfsSys.eShape.familyHistory.size() + " Sibling " + myIfsSys.eShape.shapeIndex+"/"+myIfsSys.eShape.sibsPerGen);
        ptLabel.setText(myIfsSys.theShape.pointSelected+"");
        if(inited && System.currentTimeMillis()-lastUiChange>100){
            if(myIfsSys.theShape.pointSelected !=-1){
                ifsPt editPt;
                if(myIfsSys.evolutionDescSelected){
                    editPt = myIfsSys.eShape.mutationDescriptorPt;
                    ptLabel.setText("Mutation");
                }else{
                    editPt = myIfsSys.theShape.selectedPt;
                }

                xSpinner.setValue(editPt.x);
                ySpinner.setValue(editPt.y);
                zSpinner.setValue(editPt.z);

                scaleSpinner.setValue(editPt.scale * 100);

                pitchSpinner.setValue(editPt.rotationPitch/Math.PI*180);
                yawSpinner.setValue(editPt.rotationYaw/Math.PI*180);
                rollSpinner.setValue(editPt.rotationRoll/Math.PI*180);

                camScaleSpinner.setValue((int)myIfsSys.theVolume.perspectiveScale);

                brightnessSpinner.setValue(myIfsSys.rp.brightnessMultiplier);
                samplesSpinner.setValue(myIfsSys.rp.samplesPerFrame);
                iterationsSpinner.setValue(myIfsSys.rp.iterations);
                potentialSpinner.setValue(myIfsSys.rp.potentialRadius);
                delaySpinner.setValue(myIfsSys.rp.postProcessPeriod);

                perspectiveCheck.setSelected(!myIfsSys.theVolume.usePerspective);
                frameHoldCheck.setSelected(myIfsSys.rp.holdFrame);
                gridCheck.setSelected(myIfsSys.rp.drawGrid);
                delayCheck.setSelected(myIfsSys.rp.renderThrottling);
                cartoonCheck.setSelected(myIfsSys.rp.useShadows);
                //System.out.println(renderModeCombo.setse);
                renderModeCombo.setSelectedIndex(myIfsSys.theVolume.renderMode == volume.RenderMode.PROJECT_ONLY ? 1 : 0);
                //dotSizeSpinner.setValue(myIfsSys.rp.dotSize);

                //xMinSpinner.setValue(myIfsSys.rp.xMin);
                //xMaxSpinner.setValue(myIfsSys.rp.xMax);
                //yMinSpinner.setValue(myIfsSys.rp.yMin);
                //yMaxSpinner.setValue(myIfsSys.rp.yMax);
                //zMinSpinner.setValue(myIfsSys.rp.zMin);
                //zMaxSpinner.setValue(myIfsSys.rp.zMax);
            }
        }
        /*
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
        }*/

        switch (myIfsSys.eShape.myScoreParams.myPreset){
            case MAX_SURFACE:
                //if(evolveModeCombo.getSelectedIndex()!=0)
                    evolveModeCombo.setSelectedIndex(0);
                break;
            case MAX_SurfaceVolume:
                //if(evolveModeCombo.getSelectedIndex()!=1)
                    evolveModeCombo.setSelectedIndex(1);
                break;
            case MAX_TRAVEL:
                //if(evolveModeCombo.getSelectedIndex()!=2)
                    evolveModeCombo.setSelectedIndex(2);
                break;
            case MAX_VOLUME:
                //if(evolveModeCombo.getSelectedIndex()!=3)
                    evolveModeCombo.setSelectedIndex(3);
                break;
            case MIN_DistSurface:
                //if(evolveModeCombo.getSelectedIndex()!=4)
                    evolveModeCombo.setSelectedIndex(4);
                break;
            case MIN_DistVolume:
                //if(evolveModeCombo.getSelectedIndex()!=5)
                    evolveModeCombo.setSelectedIndex(5);
                break;
            default:
                //if(evolveModeCombo.getSelectedIndex()!=6)
                    evolveModeCombo.setSelectedIndex(6);
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
        JButton wb = new WebButton();
        JButton cb = new JButton();// = (JButton)e.getSource();
        MenuItem mc = new MenuItem();// = (MenuItem)e.getSource();

        try{
            cb = (JButton)e.getSource();
        }catch (Exception _e){

        }
        try{
            wb=(WebButton)e.getSource();
        }catch (Exception _e){

        }
        try{
            mc = (MenuItem)e.getSource();
            System.out.println(mc.getActionCommand());
        }catch (Exception _e){

        }

        if(wb.getName()=="X"){
            pdfXImgFile = fc.showOpenDialog(this);
            if(pdfXImgFile == JFileChooser.APPROVE_OPTION){
                myIfsSys.thePdf.setSampleImage(fc.getSelectedFile(), pdf3D.Dimension.X);
            }
        }else if(wb.getName()=="Y"){
            pdfYImgFile = fc.showOpenDialog(this);
            if(pdfYImgFile == JFileChooser.APPROVE_OPTION){
                myIfsSys.thePdf.setSampleImage(fc.getSelectedFile(), pdf3D.Dimension.Y);
            }
        }else if(wb.getName()=="Z"){
            pdfZImgFile = fc.showOpenDialog(this);
            if(pdfZImgFile == JFileChooser.APPROVE_OPTION){
                myIfsSys.thePdf.setSampleImage(fc.getSelectedFile(), pdf3D.Dimension.Z);
            }

        }else if(wb.getName()=="parents"){
            myIfsSys.eShape.parents(myIfsSys.theShape);
            updateEvolutionTable();
        }else if(wb.getName()=="offspring"){
            myIfsSys.eShape.offSpring(myIfsSys.theShape, myIfsSys.rp.evolveIntensity);
            updateEvolutionTable();
        }else if(wb.getName()=="prevsib"){
            myIfsSys.theShape=myIfsSys.eShape.prevShape(0);
            updateEvolutionTable();
        }else if(wb.getName()=="nextsib"){
            myIfsSys.theShape=myIfsSys.eShape.nextShape(0);
            updateEvolutionTable();
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
            if (SwingUtilities.isRightMouseButton(e))
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
