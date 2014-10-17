package org.matrix.matrixandroidsdk.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.matrix.androidsdk.data.RoomState;
import org.matrix.androidsdk.data.RoomSummary;
import org.matrix.androidsdk.rest.model.Event;
import org.matrix.matrixandroidsdk.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

public class RoomSummaryAdapter extends ArrayAdapter<RoomSummary> {

    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private int mLayoutResourceId;

    private int mOddColourResId;
    private int mEvenColourResId;

    private DateFormat mDateFormat;

    /**
     * Construct an adapter which will display a list of rooms.
     * @param context Activity context
     * @param layoutResourceId The resource ID of the layout for each item. Must have TextViews with
     *                         the IDs: roomsAdapter_roomName, roomsAdapter_roomTopic
     */
    public RoomSummaryAdapter(Context context, int layoutResourceId) {
        super(context, layoutResourceId);
        mContext = context;
        mLayoutResourceId = layoutResourceId;
        mLayoutInflater = LayoutInflater.from(mContext);
        mDateFormat = new SimpleDateFormat("MMM d HH:mm", Locale.getDefault());
        setNotifyOnChange(true);
    }

    public void sortSummaries() {
        this.sort(new Comparator<RoomSummary>() {
            @Override
            public int compare(RoomSummary lhs, RoomSummary rhs) {
                if (lhs == null || lhs.getLatestEvent() == null) {
                    return 1;
                }
                else if (rhs == null || rhs.getLatestEvent() == null) {
                    return -1;
                }

                if (lhs.getLatestEvent().ts > rhs.getLatestEvent().ts) {
                    return -1;
                }
                else if (lhs.getLatestEvent().ts < rhs.getLatestEvent().ts) {
                    return 1;
                }
                return 0;
            }
        });
    }

    public void setAlternatingColours(int oddResId, int evenResId) {
        mOddColourResId = oddResId;
        mEvenColourResId = evenResId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(mLayoutResourceId, parent, false);
        }

        RoomSummary summary = getItem(position);

        String numMembers = null;
        String message = summary.getRoomTopic();
        String timestamp = null;

        TextView textView = (TextView) convertView.findViewById(R.id.roomSummaryAdapter_roomName);
        textView.setText(summary.getRoomName());

        if (summary.getNumMembers() > 0) {
            numMembers = mContext.getResources().getQuantityString(
                    R.plurals.num_members, summary.getNumMembers(),
                    summary.getNumMembers()
            );
        }

        if (summary.getLatestEvent() != null) {
            String eventType = summary.getLatestEvent().type;
            if (Event.EVENT_TYPE_MESSAGE.equals(eventType)) {
                try {
                    String body = summary.getLatestEvent().content.getAsJsonPrimitive("body").getAsString();
                    String user = summary.getLatestEvent().userId;
                    message = mContext.getString(R.string.summary_message, user, body);
                }
                catch (Exception e) {} // bad json
            }
            timestamp = mDateFormat.format(new Date(summary.getLatestEvent().ts));
        }

        textView = (TextView) convertView.findViewById(R.id.roomSummaryAdapter_message);
        textView.setText(message);
        textView = (TextView) convertView.findViewById(R.id.roomSummaryAdapter_ts);
        textView.setText(timestamp);
        textView = (TextView) convertView.findViewById(R.id.roomSummaryAdapter_numUsers);
        textView.setText(numMembers);

        if (mOddColourResId != 0 && mEvenColourResId != 0) {
            convertView.setBackgroundColor(position%2 == 0 ? mEvenColourResId : mOddColourResId);
        }

        return convertView;

    }
}
