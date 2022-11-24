package com.example.homejameswil;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;


public class AccountDetailsDriver extends Fragment
{
    TextView name, idNumber, numPlate, vehBrand, colour, cellNumber;
    View view;
    RatingBar rating;
    Button btnSignOut, btnEdit, btnExport;
    ArrayList<String> docID;

    String TAG = "Firebase";

    FirebaseUser user;
    String uid;

    int pageHeight = 1120;
    int pagewidth = 792;

    Bitmap bmp, scaledbmp;

    private static final int PERMISSION_REQUEST_CODE = 200;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_account_details_driver, container, false);
        btnExport = view.findViewById(R.id.btnExport);
        bmp = BitmapFactory.decodeResource(getResources(), R.drawable.car);
        scaledbmp = Bitmap.createScaledBitmap(bmp, 140, 140, false);
        SignOut();
        RetrieveDetails();
        rating();
        edit();

        if (checkPermission())
        {
            Toast.makeText(getContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
        } else {
            requestPermission();
        }
        btnExport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // calling method to
                // generate our PDF file.
                generatePDF();
            }
        });

        return view;
    }

    private void generatePDF()
    {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Bundle b = getArguments();

        if(b != null)
        {
           uid = b.getString("docID");
        }
        else
        {
            user = FirebaseAuth.getInstance().getCurrentUser();
            uid = user.getUid();
        }

        if(b.getString("Hide") == "Yes")
        {
            btnExport.setVisibility(View.INVISIBLE);
            btnEdit.setVisibility(View.INVISIBLE);
        }
        else
        {

        }



        //Read from database specifying with collection
        db.collection("Users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task)
            {
                if (task.isSuccessful())
                {
                    for (QueryDocumentSnapshot document : task.getResult())
                    {
                        if(document.getId().matches(uid))
                        {
                            name.setText(document.getString("UserName") + " " + document.getString("UserSurname"));
                            idNumber.setText(document.getString("UserIDNumber"));
                            cellNumber.setText(document.getString("UserCellNumber"));
                            vehBrand.setText("Brand: " + document.getString("VehicleType"));
                            numPlate.setText("Registration Plate: " + document.getString("VehicleNumPlate"));
                            colour.setText("Colour: " + document.getString("Colour"));

                            // creating an object variable
                            // for our PDF document.
                            PdfDocument pdfDocument = new PdfDocument();

                            // two variables for paint "paint" is used
                            // for drawing shapes and we will use "title"
                            // for adding text in our PDF file.
                            Paint paint = new Paint();
                            Paint title = new Paint();

                            // we are adding page info to our PDF file
                            // in which we will be passing our pageWidth,
                            // pageHeight and number of pages and after that
                            // we are calling it to create our PDF.
                            PdfDocument.PageInfo mypageInfo = new PdfDocument.PageInfo.Builder(pagewidth, pageHeight, 1).create();

                            // below line is used for setting
                            // start page for our PDF file.
                            PdfDocument.Page myPage = pdfDocument.startPage(mypageInfo);

                            // creating a variable for canvas
                            // from our page of PDF.
                            Canvas canvas = myPage.getCanvas();

                            // below line is used to draw our image on our PDF file.
                            // the first parameter of our drawbitmap method is
                            // our bitmap
                            // second parameter is position from left
                            // third parameter is position from top and last
                            // one is our variable for paint.
                            canvas.drawBitmap(scaledbmp, 56, 40, paint);

                            // below line is used for adding typeface for
                            // our text which we will be adding in our PDF file.
                            title.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));

                            // below line is used for setting text size
                            // which we will be displaying in our PDF file.
                            title.setTextSize(15);

                            // below line is sued for setting color
                            // of our text inside our PDF file.
                            title.setColor(ContextCompat.getColor(getContext(), R.color.purple_200));

                            // below line is used to draw text in our PDF file.
                            // the first parameter is our text, second parameter
                            // is position from start, third parameter is position from top
                            // and then we are passing our variable of paint which is title.

                            canvas.drawText(document.getString("UserName") + " " + document.getString("UserSurname"),209,20, title);
                            canvas.drawText(document.getString("UserIDNumber"),209,40, title);
                            canvas.drawText(document.getString("UserCellNumber"),209,60, title);
                            canvas.drawText(document.getString("VehicleType"),209,80, title);
                            canvas.drawText(document.getString("VehicleNumPlate"),209,100, title);
                            canvas.drawText(document.getString("Colour"),209,120, title);

                            // similarly we are creating another text and in this
                            // we are aligning this text to center of our PDF file.
                            title.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                            title.setColor(ContextCompat.getColor(getContext(), R.color.purple_200));
                            title.setTextSize(15);

                            // below line is used for setting
                            // our text to center of PDF.
                            /*title.setTextAlign(Paint.Align.CENTER);
                            canvas.drawText("This is sample document which we have created.", 396, 560, title);*/

                            // after adding all attributes to our
                            // PDF file we will be finishing our page.
                            pdfDocument.finishPage(myPage);

                            // below line is used to set the name of
                            // our PDF file and its path.
                            File file = new File(Environment.getExternalStorageDirectory(), document.getString("UserName") + " " + document.getString("UserSurname")+ ".pdf");

                            try {
                                // after creating a file name we will
                                // write our PDF file to that location.
                                pdfDocument.writeTo(new FileOutputStream(file));

                                // below line is to print toast message
                                // on completion of PDF generation.
                                Toast.makeText(getContext(), "PDF file generated successfully.", Toast.LENGTH_SHORT).show();
                            } catch (IOException e) {
                                // below line is used
                                // to handle error
                                e.printStackTrace();
                            }
                            // after storing our pdf to that
                            // location we are closing our PDF file.
                            pdfDocument.close();

                        }
                    }
                }
                else
                {
                    Log.w(TAG, "Error getting documents.", task.getException());
                }
            }
        });


    }

    private boolean checkPermission() {
        // checking of permissions.
        int permission1 = ContextCompat.checkSelfPermission(getContext(), WRITE_EXTERNAL_STORAGE);
        int permission2 = ContextCompat.checkSelfPermission(getContext(), READ_EXTERNAL_STORAGE);
        return permission1 == PackageManager.PERMISSION_GRANTED && permission2 == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        // requesting permissions if not provided.
        ActivityCompat.requestPermissions(getActivity(), new String[]{WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0) {

                // after requesting permissions we are showing
                // users a toast message of permission granted.
                boolean writeStorage = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean readStorage = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                if (writeStorage && readStorage) {
                    Toast.makeText(getContext(), "Permission Granted..", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Permission Denied.", Toast.LENGTH_SHORT).show();
                    //finish();
                }
            }
        }
    }


    private void RetrieveDetails()
    {
        name = view.findViewById(R.id.nameDriver);
        idNumber = view.findViewById(R.id.idNumberDriver);
        rating = view.findViewById(R.id.ratingBar);
        numPlate = view.findViewById(R.id.numPlate);
        vehBrand = view.findViewById(R.id.vehBrand);
        colour = view.findViewById(R.id.vehColour);
        cellNumber = view.findViewById(R.id.cellNumber);
        btnEdit = view.findViewById(R.id.btnEdit);

        //Connection to database
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Bundle bundle = this.getArguments();

        //Jared

        if(bundle != null)
        {
            btnSignOut.setVisibility(View.GONE);
            bundle.getString("docID");
            bundle.getString("userID");

            uid = bundle.getString("docID");
        }
        else
        {
            btnEdit.setVisibility(View.GONE);
            btnExport.setVisibility(View.GONE);
            // Get logged in user UID
            user = FirebaseAuth.getInstance().getCurrentUser();
            uid = user.getUid();
        }

                //Read from database specifying with collection
        db.collection("Users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
                {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task)
                    {
                        if (task.isSuccessful())
                        {
                            for (QueryDocumentSnapshot document : task.getResult())
                            {
                                if(document.getId().matches(uid))
                                {
                                    name.setText(document.getString("UserName") + " " + document.getString("UserSurname"));
                                    idNumber.setText(document.getString("UserIDNumber"));
                                    cellNumber.setText(document.getString("UserCellNumber"));
                                    vehBrand.setText("Brand: " + document.getString("VehicleType"));
                                    numPlate.setText("Registration Plate: " + document.getString("VehicleNumPlate"));
                                    colour.setText("Colour: " + document.getString("Colour"));
                                }
                            }
                        }
                        else
                        {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    private void rating()
    {

        rating.setRating(3.25f);
    }

    private void edit()
    {
        btnEdit = view.findViewById(R.id.btnEdit);

        btnEdit.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Bundle b = getArguments();
                String dUID = b.getString("docID");
                Bundle bundle = new Bundle();
                bundle.putString("docID", dUID);
                //bundle.putString("userID", "True");

                //String DocumentID = docID.get(position);

                Fragment fragment = new EditDriverInfo();
                fragment.setArguments(bundle);
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.frameLayout, fragment);
                ft.commit();
            }
        });
    }

    private void SignOut()
    {
        btnSignOut = view.findViewById(R.id.btnSignOut);
        btnSignOut.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getActivity(), Login.class);
                startActivity(intent);
            }
        });
    }
}
