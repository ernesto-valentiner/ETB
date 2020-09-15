/*
 an auto-generated user wrapper template for the service 'getFGModeSelectorSR'
*/

package evidentia.wrappers;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import evidentia.configurations.*;


public class getFGModeSelectorSRWRP extends getFGModeSelectorSRETBWRP {

    @Override
    public void run(){
        if (mode.equals("+-")) {
            String path = "/Users/Ernesto/BachelorThesis/etb_org2/TempRepo/evidence";
            out2 = path + "/FGModeSelectorSR.pdf";
            try {

                //create document supporting fulfillment of safety requirements
                Document document = new Document();
                PdfWriter.getInstance(document, new FileOutputStream(path + "/FGModeSelectorSR.pdf"));

                document.open();

                Font font = FontFactory.getFont(FontFactory.COURIER, 16, BaseColor.BLACK);
                Font bold = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);

                Chunk chunk = new Chunk("Flight Guidance Mode Selector Safety Requirements \n", font);
                document.add(chunk);


                //verification of the component based on the requirements
                FGModeSelector selector = new FGModeSelector(true);

                if(!selector.isModeAnnunciationsOn()){

                    Paragraph p1 = new Paragraph("\nAnnunciation Operation: \n", bold);
                    Paragraph p2 = new Paragraph("The mode annunciations are not on at system power up.\n");
                    document.add(p1);
                    document.add(p2);
                }

                selector.turnOnModeAnnunciations();
                if(selector.isModeAnnunciationsOn()){

                    Paragraph p3 = new Paragraph("\nAnnunciation Selection: \n", bold);
                    Paragraph p4 = new Paragraph("The mode annunciations are turned on when the onside FD is turned on, this side is active and the mode annunciations are off. \n");
                    document.add(p3);
                    document.add(p4);
                }

                selector.turnOffModeAnnunciations();
                if(!selector.isModeAnnunciationsOn()){

                    Paragraph p5 = new Paragraph("\nAnnunciation De-Selection: \n", bold);
                    Paragraph p6 = new Paragraph("The mode annunciations are turned off if the onside FD is off, the offside FD is off, the AP is disengaged, this side is active and the mode annunciations are turned on.\n");
                    document.add(p5);
                    document.add(p6);
                }
                document.close();


            } catch (IOException e) {
                e.printStackTrace();
            } catch (DocumentException e) {
                e.printStackTrace();
            }
        }
        else {
            System.out.println("unrecognized mode for getFGModeSelectorSR");
        }
    }
}