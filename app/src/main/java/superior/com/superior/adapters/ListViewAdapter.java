package superior.com.superior.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import superior.com.superior.R;
import superior.com.superior.models.FarmerNames;

public class ListViewAdapter extends BaseAdapter {

    // Declare Variables

    Context mContext;
    LayoutInflater inflater;
    private List<FarmerNames> farmerList = null;
    private ArrayList<FarmerNames> arraylist;

    public ListViewAdapter(Context context, List<FarmerNames> animalNamesList) {
        mContext = context;
        this.farmerList = animalNamesList;
        inflater = LayoutInflater.from(mContext);
        this.arraylist = new ArrayList<FarmerNames>();
        this.arraylist.addAll(animalNamesList);
    }

    public class ViewHolder {
        TextView name;
    }

    @Override
    public int getCount() {
        return farmerList.size();
    }

    @Override
    public FarmerNames getItem(int position) {
        return farmerList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View view, ViewGroup parent) {
        final ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = inflater.inflate(R.layout.list_view_items, null);
            // Locate the TextViews in listview_item.xml
            holder.name = (TextView) view.findViewById(R.id.name);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        // Set the results into TextViews
        holder.name.setText(farmerList.get(position).getSupplier_name());
        return view;
    }


    // Filter Class
    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        farmerList.clear();
        if (charText.length() == 0) {
            farmerList.addAll(arraylist);
        } else {
            for (FarmerNames wp : arraylist) {
                if (wp.getSupplier_name().toLowerCase(Locale.getDefault()).contains(charText) ||
                        wp.getSupplier_id().toLowerCase(Locale.getDefault()).contains(charText) ) {
                    farmerList.add(wp);
                }
            }
        }
        notifyDataSetChanged();
    }
}