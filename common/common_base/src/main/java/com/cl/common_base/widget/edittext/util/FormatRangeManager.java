package com.cl.common_base.widget.edittext.util;

import com.cl.common_base.widget.edittext.bean.FormatItemResult;
import com.cl.common_base.widget.edittext.bean.FormatRange;
import com.cl.common_base.widget.edittext.bean.FormatResult;
import com.cl.common_base.widget.edittext.bean.MentionTopic;
import com.cl.common_base.widget.edittext.bean.MentionUser;
import com.cl.common_base.widget.edittext.bean.Range;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FormatRangeManager extends RangeManager {

    public FormatResult getFormatResult(String text) {
        FormatResult result = new FormatResult();
        result.setText(text);
        if (isEmpty()) {
            return result;
        }

        ArrayList<? extends Range> ranges = get();
        Collections.sort(ranges);

        List<FormatItemResult> userResultList = new ArrayList<>();
        List<FormatItemResult> topicResultList = new ArrayList<>();
        for (Range range : ranges) {
            if (range instanceof FormatRange) {
                FormatRange formatRange = (FormatRange) range;
                FormatRange.FormatData convert = formatRange.getConvert();
                if (formatRange.getInsertData() instanceof MentionUser) {
                    FormatItemResult userResult = (FormatItemResult) convert.formatResult();
                    userResult.setFromIndex(range.getFrom());
                    userResult.setLength(range.getTo() - range.getFrom());
                    userResultList.add(userResult);
                } else if (formatRange.getInsertData() instanceof MentionTopic) {
                    FormatItemResult topicResult = (FormatItemResult) convert.formatResult();
                    topicResult.setFromIndex(range.getFrom());
                    topicResult.setLength(range.getTo() - range.getFrom());
                    topicResultList.add(topicResult);
                }
            }
        }
        result.setUserList(userResultList);
        result.setTopicList(topicResultList);
        return result;
    }
}