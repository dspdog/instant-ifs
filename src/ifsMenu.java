import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.multi.MultiSplitPaneUI;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class ifsMenu implements ItemListener, ChangeListener {

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

    JSpinner pitchSpinner;
    JSpinner yawSpinner;

    JSpinner scaleSpinner;

    JCheckBox frameHoldCheck;

    boolean inited=false;
    boolean autoChange = false;
    long lastUiChange = 0;

    public void addLabeledSpinner(JSpinner spinner, SpringLayout layout, String labelText, JPanel panel, int row){
        int spinnerLeft = 70;
        int spinnerRight = -5;
        int labelToSpinner = -5;
        int vspace = 20;
        int topPad=5;

        JLabel label = new JLabel(labelText);

        layout.putConstraint(SpringLayout.EAST, label, labelToSpinner, SpringLayout.WEST, spinner);
        layout.putConstraint(SpringLayout.WEST, spinner, spinnerLeft, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.EAST, spinner, spinnerRight, SpringLayout.EAST, panel);
        layout.putConstraint(SpringLayout.NORTH, spinner, topPad+vspace*row, SpringLayout.NORTH, panel);
        layout.putConstraint(SpringLayout.NORTH, label, topPad+vspace*row, SpringLayout.NORTH, panel);

        spinner.addChangeListener(this);

        panel.add(label);
        panel.add(spinner);
    }

    public void addLabeledCheckbox(JCheckBox checkbox, SpringLayout layout, String labelText, JPanel panel, int row){
        int spinnerLeft = 70;
        int spinnerRight = -5;
        int labelToSpinner = -5;
        int vspace = 20;
        int topPad=5;

        JLabel label = new JLabel(labelText);

        layout.putConstraint(SpringLayout.EAST, label, labelToSpinner, SpringLayout.WEST, checkbox);
        layout.putConstraint(SpringLayout.WEST, checkbox, spinnerLeft, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.EAST, checkbox, spinnerRight, SpringLayout.EAST, panel);
        layout.putConstraint(SpringLayout.NORTH, checkbox, topPad+vspace*row, SpringLayout.NORTH, panel);
        layout.putConstraint(SpringLayout.NORTH, label, topPad+vspace*row, SpringLayout.NORTH, panel);

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

    public void setupRenderPropertiesPanel(JPanel panel){
        brightnessSpinner = new JSpinner();
        samplesSpinner = new JSpinner();
        iterationsSpinner = new JSpinner();
        frameHoldCheck = new JCheckBox();

        JLabel renderLabel = new JLabel(" Render Properties");

        int topPad=5;

        SpringLayout layout = new SpringLayout();
        panel.setLayout(layout);

        layout.putConstraint(SpringLayout.NORTH, ptLabel, topPad, SpringLayout.NORTH, panel);

        addLabeledSpinner(brightnessSpinner, layout, "Brightness", panel, 1);
        addLabeledSpinner(samplesSpinner, layout, "Dots/Frame", panel, 2);
        addLabeledSpinner(iterationsSpinner, layout, "Iterations", panel, 3);
        addLabeledCheckbox(frameHoldCheck, layout, "Hold Frame", panel, 4);

        panel.add(renderLabel);
    }

    public ifsMenu(Frame f, ifsys is, JPanel sideMenu){

        inited=false;

        myIfsSys = is;
        JPanel pointProperties = new JPanel();
        JPanel renderProperties = new JPanel();
        JPanel cameraProperties = new JPanel();

        //SIDE MENU

        setupPointPropertiesPanel(pointProperties);
        setupRenderPropertiesPanel(renderProperties);

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
        splitPaneBig.setBottomComponent(cameraProperties);
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

                frameHoldCheck.setSelected(myIfsSys.holdFrame);
            }
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

            myIfsSys.iterations = Integer.parseInt(iterationsSpinner.getValue().toString());
            myIfsSys.brightnessMultiplier = Double.parseDouble(brightnessSpinner.getValue().toString());
            myIfsSys.samplesPerFrame = Double.parseDouble(samplesSpinner.getValue().toString());

            myIfsSys.holdFrame = frameHoldCheck.isSelected();

            myIfsSys.shape.updateCenter();
            myIfsSys.clearframe();

        }
    }
}
