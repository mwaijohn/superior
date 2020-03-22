package superior.com.superior;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.CancellationSignal;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.PrintManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Print extends AppCompatActivity {

    Button btnPrint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print);

        btnPrint = (Button) findViewById(R.id.printdoc);

        genPDFReport();

        btnPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PrintManager printManager = (PrintManager) Print.this.getSystemService(Context.PRINT_SERVICE);
                try
                {
                    File root = new File(Environment.getExternalStorageDirectory(), "weight");
                    File file = new File(root,"report.pdf");

                    PrintDocumentAdapter printAdapter = new PdfDocumentAdapter(Print.this,file.toString() );
                    printManager.print("printing report", printAdapter,new PrintAttributes.Builder().build());
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });

    }

    //generate pdf report
    public void genPDFReport() {
        Document document = new Document(PageSize.A4);

        File root = new File(Environment.getExternalStorageDirectory(), "weight");
        File file = new File(root,"report.pdf");

        //get content
        String massage = "";

        //get massage body
        SharedPreferences details = getSharedPreferences("APP_DETAILS",Context.MODE_PRIVATE);

        String reading = details.getString("reading",""); //weight readings
        String shift = details.getString("shift","");
        String farmer_id = details.getString("supplier_id","");
        String farmer_name = details.getString("supp_name","");
        String grader_name = details.getString("grader_name","");


        DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        String date = df.format(new Date()).toString();

        massage += farmer_name + ": " + farmer_id + "\n";
        massage += "Quantity supplied: " + reading.trim() + "\n";
        massage += "Collected by: " + grader_name + "\n";
        massage += shift + " shift on " + date;

        try {
            PdfWriter.getInstance(document,new FileOutputStream(file));
            document.open();
        } catch (DocumentException | FileNotFoundException e) {
            e.printStackTrace();
        }
        //create paragraphs
        //Paragraph para = new Paragraph("Second paragraph");
        //content
        Paragraph content = new Paragraph(massage);
        try {
            //document.add(para);
            document.add(content);
            Toast.makeText(getApplicationContext(),"Data written to pdf",Toast.LENGTH_LONG).show();
        } catch (DocumentException e) {
            e.printStackTrace();
        }

        document.close();
    }

    public class PdfDocumentAdapter extends PrintDocumentAdapter {

        Context context = null;
        String pathName = "";
        public PdfDocumentAdapter(Context ctxt, String pathName) {
            context = ctxt;
            this.pathName = pathName;
        }
        @Override
        public void onLayout(PrintAttributes printAttributes, PrintAttributes printAttributes1, CancellationSignal cancellationSignal, LayoutResultCallback layoutResultCallback, Bundle bundle) {
            if (cancellationSignal.isCanceled()) {
                layoutResultCallback.onLayoutCancelled();
            }
            else {
                PrintDocumentInfo.Builder builder=
                        new PrintDocumentInfo.Builder("report.pdf");
                builder.setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                        .setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
                        .build();
                layoutResultCallback.onLayoutFinished(builder.build(),
                        !printAttributes1.equals(printAttributes));
            }
        }

        @Override
        public void onWrite(PageRange[] pageRanges, ParcelFileDescriptor parcelFileDescriptor, CancellationSignal cancellationSignal, WriteResultCallback writeResultCallback) {
            InputStream in=null;
            OutputStream out=null;
            File root = new File(Environment.getExternalStorageDirectory(), "weight");
            try {
                File file = new File(root,"report.pdf");
                in = new FileInputStream(file);
                out=new FileOutputStream(parcelFileDescriptor.getFileDescriptor());

                byte[] buf=new byte[16384];
                int size;

                while ((size=in.read(buf)) >= 0
                        && !cancellationSignal.isCanceled()) {
                    out.write(buf, 0, size);
                }

                if (cancellationSignal.isCanceled()) {
                    writeResultCallback.onWriteCancelled();
                }
                else {
                    writeResultCallback.onWriteFinished(new PageRange[] { PageRange.ALL_PAGES });
                }
            }
            catch (Exception e) {
                writeResultCallback.onWriteFailed(e.getMessage());
                //Logger.logError( e);
            }
            finally {
                try {
                    in.close();
                    out.close();
                }
                catch (IOException e) {
                    //Logger( e);
                }
            }
        }}
}
