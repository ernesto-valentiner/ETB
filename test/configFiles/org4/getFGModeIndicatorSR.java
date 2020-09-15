/*
 an auto-generated user wrapper template for the service 'getFGModeIndicatorSR'
*/

package evidentia.wrappers;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import evidentia.configurations.FGModeIndicator;
import java.io.FileOutputStream;
import java.io.IOException;

public class getFGModeIndicatorSRWRP extends getFGModeIndicatorSRETBWRP {

    @Override
    public void run(){
        if (mode.equals("+-")) {

            String path = "/Users/Ernesto/BachelorThesis/etb_org2/TempRepo/evidence";
            out2 = path + "/FGModeIndicatorSR.pdf";

            try {


                //create document supporting fulfillment of safety requirements
                Document document = new Document();
                PdfWriter.getInstance(document, new FileOutputStream(path + "/FGModeIndicatorSR.pdf"));

                document.open();

                Font font = FontFactory.getFont(FontFactory.COURIER, 16, BaseColor.BLACK);
                Font bold = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);

                Chunk chunk = new Chunk("Flight Guidance Mode Indicator Safety Requirements \n", font);

                document.add(chunk);


                //verification of the component based on the requirements
                FGModeIndicator indicator = new FGModeIndicator();

                if(indicator.getLateralMode().equals("Roll Hold")){

                    Paragraph p1 = new Paragraph("\nLateral Modes Operation: \n", bold);
                    Paragraph p2 = new Paragraph("The default lateral mode is Roll Hold.\n");
                    document.add(p1);
                    document.add(p2);
                }

                indicator.changeLateralMode("Approach");
                if(indicator.getLateralMode().equals("Approach")){

                    Paragraph p3 = new Paragraph("\nLateral Modes Operation: \n", bold);
                    Paragraph p4 = new Paragraph("Only one lateral mode is ever active at any time.\n");
                    document.add(p3);
                    document.add(p4);
                }


                if(indicator.getVerticalMode().equals("Pitch Hold")){

                    Paragraph p5 = new Paragraph("\nVertical Modes Operation: \n", bold);
                    Paragraph p6 = new Paragraph("The default lateral mode is Pitch Hold.\n");
                    document.add(p5);
                    document.add(p6);
                }

                indicator.changeVerticalMode("Altitude Select");
                if(indicator.getLateralMode().equals("Altitude Select")){

                    Paragraph p7 = new Paragraph("\nVertical Modes Operation: \n", bold);
                    Paragraph p8 = new Paragraph("Only one vertical mode shall ever be active at any time.\n");
                    document.add(p7);
                    document.add(p8);
                }

                document.close();


            } catch (IOException e) {
                e.printStackTrace();
            } catch (DocumentException e) {
                e.printStackTrace();
            }
        }
        else {
            System.out.println("unrecognized mode for getFGModeIndicatorSR");
        }
    }
}