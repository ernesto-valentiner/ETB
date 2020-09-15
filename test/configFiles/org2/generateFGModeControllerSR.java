/*
 an auto-generated user wrapper template for the service 'generateFGModeControllerSR'
*/

package evidentia.wrappers;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSmartCopy;
import java.io.FileOutputStream;
import java.io.IOException;

public class generateFGModeControllerSRWRP extends generateFGModeControllerSRETBWRP {

    @Override
    public void run(){
        if (mode.equals("+-")) {
            String path = "/Users/Ernesto/BachelorThesis/etb_org2/TempRepo/evidence";
            out2 = path + "/FGModeControllerSR.pdf";
            try {
                Font font = FontFactory.getFont(FontFactory.COURIER, 16, BaseColor.BLACK);

                Document document = new Document();
                PdfCopy copy = new PdfSmartCopy(document, new FileOutputStream(path + "/FGModeControllerSR.pdf"));

                document.open();

                Chunk chunk = new Chunk("Flight Guidance Mode Controller Safety Requirements \n", font);

                copy.add(chunk);

                copy.addDocument(new PdfReader( path + "/FGModeSelectorSR.pdf"));
                copy.addDocument(new PdfReader(path + "/FGModeIndicatorSR.pdf"));


                document.close();

            } catch (IOException e) {
                e.printStackTrace();
            } catch (DocumentException e) {
                e.printStackTrace();
            }
        }
        else {
            System.out.println("unrecognized mode for generateFGModeControllerSR");
        }
    }
}