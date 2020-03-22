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
import superior.com.superior.models.RouteNames;

public class RoutesAdapter extends BaseAdapter {

    Context mContext;
    LayoutInflater inflater;
    private List<RouteNames> routeList = null;
    private ArrayList<RouteNames> arraylist;

    public RoutesAdapter(Context context, List<RouteNames> routesList) {
        mContext = context;
        this.routeList = routesList;
        inflater = LayoutInflater.from(mContext);
        this.arraylist = new ArrayList<RouteNames>();
        this.arraylist.addAll(routesList);
    }

    public class ViewHolder {
        TextView name;
    }

    @Override
    public int getCount() {
        return routeList.size();
    }

    @Override
    public RouteNames getItem(int position) {
        return routeList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        final RoutesAdapter.ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = inflater.inflate(R.layout.list_view_items, null);
            // Locate the TextViews in listview_item.xml
            holder.name = (TextView) view.findViewById(R.id.name);
            view.setTag(holder);
        } else {
            holder = (RoutesAdapter.ViewHolder) view.getTag();
        }
        // Set the results into TextViews
        holder.name.setText(routeList.get(position).getName());
        return view;
    }

    // Filter Class
    public void filter(String charText) {
        charText = charText.toUpperCase(Locale.getDefault());
        routeList.clear();
        if (charText.length() == 0) {
            routeList.addAll(arraylist);
        } else {
            for (RouteNames wp : arraylist) {
                if (wp.getName().toUpperCase(Locale.getDefault()).contains(charText)  ) {
                    routeList.add(wp);
                }
            }
        }
        notifyDataSetChanged();
    }


}
