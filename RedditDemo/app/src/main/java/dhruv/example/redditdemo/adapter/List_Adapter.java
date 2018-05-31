package dhruv.example.redditdemo.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import dhruv.example.redditdemo.R;
import dhruv.example.redditdemo.model.Reddit;

public class List_Adapter extends ArrayAdapter<Reddit> {
    List<Reddit> redditList;
    Context context;

    public List_Adapter(List<Reddit> redditList, Context context) {
        super(context, R.layout.row_list,redditList);
        this.context=context;
        this.redditList=redditList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

       ViewHolder viewHolder;

       if(convertView==null){

           LayoutInflater inflater=LayoutInflater.from(context);
           convertView=inflater.inflate(R.layout.row_list,null,true);
           viewHolder=new ViewHolder(convertView);
           convertView.setTag(viewHolder);

       }else {
           viewHolder=(ViewHolder)convertView.getTag();
       }

        Reddit reddit=redditList.get(position);

        viewHolder.score.setText(String.valueOf(reddit.getScore()));
        viewHolder.authore.setText(reddit.getAuthor());
        viewHolder.title.setText(reddit.getTitle());

        return convertView;
    }

    class ViewHolder{
        TextView score,authore,title;
        ViewHolder(View itemView){
            score=itemView.findViewById(R.id.tv_score);
            title=itemView.findViewById(R.id.tv_title);
            authore=itemView.findViewById(R.id.tv_author);
        }
    }
}
