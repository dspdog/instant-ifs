import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class ifsMenu implements ItemListener, ActionListener{

    ifsys myIfsSys;

    CheckboxMenuItem topButton;
    CheckboxMenuItem sideButton;
    CheckboxMenuItem frontButton;

    public ifsMenu(Frame f, ifsys is){

        myIfsSys = is;

        MenuBar menuBar;
        Menu pdfMenu, renderMenu, shapeMenu, guidesMenu, viewMenu;

        menuBar = new MenuBar();
        pdfMenu = new Menu("PDF");
        renderMenu = new Menu("Render");
        shapeMenu = new Menu("Shape");
        guidesMenu = new Menu("Guides");
        viewMenu = new Menu("View");

        //RENDER MENU
            CheckboxMenuItem aaButton = new CheckboxMenuItem("Anti-Aliasing"); //anti-aliasing toggle
            aaButton.setState(is.antiAliasing);
            aaButton.addItemListener(this);
            renderMenu.add(aaButton);

        //VIEW MENU

        //SHAPE MENU
            CheckboxMenuItem autoScaleButton = new CheckboxMenuItem("AutoScale Points"); //autoscale toggle
            autoScaleButton.setState(is.shape.autoScale);
            autoScaleButton.addItemListener(this);
            shapeMenu.add(autoScaleButton);

            CheckboxMenuItem imgButton = new CheckboxMenuItem("PDF Samples"); //img samples toggle
            imgButton.setState(is.usePDFSamples);
            imgButton.addItemListener(this);
            shapeMenu.add(imgButton);

            CheckboxMenuItem leavesButton = new CheckboxMenuItem("Leaves"); //leaves toggle
            leavesButton.setState(!is.leavesHidden);
            leavesButton.addItemListener(this);
            shapeMenu.add(leavesButton);

            CheckboxMenuItem trailsButton = new CheckboxMenuItem("Point Trails"); //trails toggle
            trailsButton.setState(!is.trailsHidden);
            trailsButton.addItemListener(this);
            shapeMenu.add(trailsButton);

        //GUIDES MENU
            CheckboxMenuItem infoButton = new CheckboxMenuItem("Info Box"); //info box toggle
            infoButton.setState(!is.infoHidden);
            infoButton.addItemListener(this);
            guidesMenu.add(infoButton);

            menuBar.add(renderMenu);
            menuBar.add(shapeMenu);
            //menuBar.add(pdfMenu);
            menuBar.add(guidesMenu);
            menuBar.add(viewMenu);

            f.setMenuBar(menuBar);
    }

    public void itemStateChanged(ItemEvent e) {
        //RENDER MENU
            if(e.getItem()=="Anti-Aliasing"){
                myIfsSys.antiAliasing = e.getStateChange()==1;
            }
        //VIEW MENU

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
            if(e.getItem()=="Leaves"){
                myIfsSys.leavesHidden = e.getStateChange()==2;
            }
            if(e.getItem()=="Point Trails"){
                myIfsSys.spokesHidden = e.getStateChange()==2;
            }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        //if(e.getActionCommand()=="Center on Screen"){
        //    myIfsSys.centerOnGrav();
        //}
    }
}
