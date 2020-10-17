/**
 * 
 */
package es.upv.grc.easymanet.adapters;

import java.util.List;

import es.upv.grc.easymanet.android.R;
import es.upv.grc.easymanet.android.User;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * @author LSMS
 *
 */
public class UserAdapter extends ArrayAdapter<User> {

	LayoutInflater mInflater;
	public UserAdapter(Context context, int resource, int textViewResourceId,
			List<User> objects) {
		super(context, resource, textViewResourceId, objects);
		// TODO Auto-generated constructor stub
		mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {

		RelativeLayout view;
		TextView nodoTV;
		ImageButton nodoIV;
		
        if (convertView == null) {  // Si no es reciclada, la inicializamos
            view = (RelativeLayout)mInflater.inflate(R.layout.nodo_layout, parent);
            view.setLayoutParams(new GridView.LayoutParams(85, 85));
            view.setPadding(8, 8, 8, 8);
        } else {
            view = (RelativeLayout) convertView;
        }

        User user = getItem(position);
        nodoTV = (TextView) view.findViewById(R.id.nodoTextView);
        nodoIV = (ImageButton) view.findViewById(R.id.nodoImageButton);
        
        nodoIV.setPadding(2, 2, 2, 2);
        nodoIV.setScaleType(ImageButton.ScaleType.CENTER_CROP);
        /* Populate the row's xml with info from the item */
        nodoTV.setText(user.getNick());
        nodoIV.setImageBitmap(user.getImgBmp());
//        nodoIV.setImageResource(mThumbIds[position]);
        return view;
    }



}
