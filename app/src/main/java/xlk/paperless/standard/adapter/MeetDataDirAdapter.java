package xlk.paperless.standard.adapter;

import androidx.annotation.Nullable;

import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.mogujie.tt.protobuf.InterfaceFile;

import java.util.List;

import xlk.paperless.standard.R;

/**
 * @author xlk
 * @date 2020/3/14
 * @desc 会议资料中目录adapter
 */
public class MeetDataDirAdapter extends BaseQuickAdapter<InterfaceFile.pbui_Item_MeetDirDetailInfo, BaseViewHolder> {
    private int chooseId = -1;

    public MeetDataDirAdapter(int layoutResId, @Nullable List<InterfaceFile.pbui_Item_MeetDirDetailInfo> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, InterfaceFile.pbui_Item_MeetDirDetailInfo item) {
        helper.setText(R.id.item_meet_data_dir_name, item.getName().toStringUtf8());
        boolean selected = item.getId() == chooseId;
        helper.getView(R.id.item_meet_data_dir_iv).setSelected(selected);
        helper.getView(R.id.item_meet_data_dir_ll).setSelected(selected);
        TextView view = helper.getView(R.id.item_meet_data_dir_name);
        view.setTextColor(selected ? getContext().getResources().getColor(R.color.white) : getContext().getResources().getColor(R.color.light_black));
    }

    public int getChooseId() {
        return chooseId;
    }

    public void setChoose(int dirId) {
        chooseId = dirId;
        notifyDataSetChanged();
    }
}
