package com.mobilevue.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

import com.mobilevue.data.PlanDatum;
import com.mobilevue.vod.NewPackageFragment;
import com.mobilevue.vod.R;

public class NewPackageAdapter extends BaseExpandableListAdapter {

	private Context _context;
	private List<PlanDatum> _planData;
	private ArrayList<RadioButton> _arrRadioButton;
	private int rb_seed = 9001;
	private NewPackageFragment frag;

	public NewPackageAdapter(Context context, List<PlanDatum> planData,NewPackageFragment frag) {
		this._context = context;
		this._planData = planData;
		this._arrRadioButton = new ArrayList<RadioButton>();
		this.frag = frag;
	}

	@Override
	public Object getChild(int groupPosition, int childPosititon) {
		return this._planData.get(groupPosition).getServices()
				.get(childPosititon).getServiceDescription();
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public View getChildView(int groupPosition, final int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {

		final String childText = (String) getChild(groupPosition, childPosition);

		if (convertView == null) {
			LayoutInflater infalInflater = (LayoutInflater) this._context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = infalInflater.inflate(R.layout.f_plan_list_item, null);
		}

		TextView txtListChild = (TextView) convertView
				.findViewById(R.id.f_plan_list_item_tv);

		txtListChild.setText(childText);
		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return this._planData.get(groupPosition).getServices().size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return this._planData.get(groupPosition).getPlanDescription();
	}

	@Override
	public int getGroupCount() {
		return this._planData.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public View getGroupView(final int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		String headerTitle = (String) getGroup(groupPosition);
		if (convertView == null) {
			LayoutInflater infalInflater = (LayoutInflater) this._context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = infalInflater.inflate(R.layout.f_plan_list_group, null);
		}
		TextView lblListHeader = (TextView) convertView
				.findViewById(R.id.f_plan_list_plan_tv);
		lblListHeader.setTypeface(null, Typeface.BOLD);
		lblListHeader.setText(headerTitle);

		RadioButton rb1 = (RadioButton) convertView
				.findViewById(R.id.f_plan_list_plan_rb);
		rb1.setTag(rb_seed + groupPosition);
		_arrRadioButton.add(rb1);
		rb1.setFocusable(false);

		rb1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				for (RadioButton rb : _arrRadioButton) {
					if (rb.getTag() != v.getTag()) {
						rb.setChecked(false);
					}
				}
				frag.selectedGroupItem = groupPosition;
			}
		});
		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
}