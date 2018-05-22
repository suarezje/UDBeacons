package co.edu.ingsw.udstrital.udbeacons.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import co.edu.ingsw.udstrital.udbeacons.R;
import co.edu.ingsw.udstrital.udbeacons.activities.MainActivity;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DayFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DayFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DayFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "dayOfWeek";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private int dayOfWeek;
    private String mParam2;

    private Context context;
    private SharedPreferences sharedPref;

    private Map<Integer,String> mapDaysOfWeek;

    private OnFragmentInteractionListener mListener;

    public DayFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DayFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DayFragment newInstance(int param1, String param2) {
        DayFragment fragment = new DayFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            dayOfWeek = getArguments().getInt(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        this.context = this.getContext();
        this.sharedPref = this.context.getSharedPreferences(
                getString(R.string.shared_preference_file), Context.MODE_PRIVATE);

        mapDaysOfWeek = new HashMap<>();
        mapDaysOfWeek.put(0,"Monday");
        mapDaysOfWeek.put(1,"Tuesday");
        mapDaysOfWeek.put(2,"Wednesday");
        mapDaysOfWeek.put(3,"Thursday");
        mapDaysOfWeek.put(4,"Friday");
        mapDaysOfWeek.put(5,"Saturday");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        String dayOfThisTab = mapDaysOfWeek.get(dayOfWeek);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_day, container, false);

        JSONArray jsonSchedulle = null;
        try {
            jsonSchedulle = new JSONArray(this.sharedPref.getString("user_schedulle",""));
            if(jsonSchedulle != null && dayOfThisTab != null){
                TableLayout tableSchedulle = (TableLayout)view.findViewById(R.id.tableSchedulle);
                TableRow tableRow = new TableRow(getActivity());
                TableLayout.LayoutParams tableRowParams= new TableLayout.LayoutParams
                        (TableLayout.LayoutParams.MATCH_PARENT,TableLayout.LayoutParams.MATCH_PARENT);
                for(int i=0; i < jsonSchedulle.length(); i++){
                    JSONObject row = jsonSchedulle.getJSONObject(i);
                    if(row != null && row.getString("day").toUpperCase().equals(dayOfThisTab.toUpperCase())){

                        tableRow.setLayoutParams(tableRowParams);
                        EditText subject = new EditText(getActivity());
                        subject.setEms(5);
                        subject.setEnabled(false);
                        subject.setBackgroundColor(0);
                        subject.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);

                        EditText teacher = new EditText(getActivity());
                        teacher.setEms(5);
                        teacher.setEnabled(false);
                        teacher.setBackgroundColor(0);
                        teacher.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);

                        EditText startTime = new EditText(getActivity());
                        startTime.setEms(5);
                        startTime.setEnabled(false);
                        startTime.setBackgroundColor(0);
                        startTime.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);

                        EditText endTime = new EditText(getActivity());
                        endTime.setEms(5);
                        endTime.setEnabled(false);
                        endTime.setBackgroundColor(0);
                        endTime.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);

                        EditText classRoom = new EditText(getActivity());
                        classRoom.setEms(5);
                        classRoom.setEnabled(false);
                        classRoom.setBackgroundColor(0);
                        classRoom.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);

                        subject.setText(row.getString("subjectName"));
                        teacher.setText(row.getString("teacher"));
                        startTime.setText(row.getString("timeStart"));
                        endTime.setText(row.getString("timeEnd"));
                        classRoom.setText(row.getString("classRoom"));
                        tableRow.addView(subject);
                        tableRow.addView(teacher);
                        tableRow.addView(startTime);
                        tableRow.addView(endTime);
                        tableRow.addView(classRoom);
                    }
                }
                tableSchedulle.addView(tableRow);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }



        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
