package org.embulk.input.kintone;

import com.kintone.client.model.FileBody;
import com.kintone.client.model.Group;
import com.kintone.client.model.Organization;
import com.kintone.client.model.User;
import com.kintone.client.model.record.Record;
import com.kintone.client.model.record.TableRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.Gson;

import java.util.List;

public class KintoneAccessor {
    private final Logger logger = LoggerFactory.getLogger(KintoneAccessor.class);
    private final Gson gson = new Gson();

    private final Record record;
    private final String delimiter = "\n";

    public KintoneAccessor(final Record record) {
        this.record = record;
    }

    public String get(String name) {
        if (name.equals("$id")) {
            return record.getId().toString();
        }
        if (name.equals("$revision")) {
            return record.getRevision().toString();
        }
        switch (this.record.getFieldType(name)) {
            case USER_SELECT:
                List<User> users1 = this.record.getUserSelectFieldValue(name);
                return usersToString(users1);
            case ORGANIZATION_SELECT:
                List<Organization> organizations = this.record.getOrganizationSelectFieldValue(name);
                return organizations.stream().map(Organization::getCode)
                        .reduce((accum, value) -> accum + this.delimiter + value)
                        .orElse("");
            case GROUP_SELECT:
                List<Group> groups = this.record.getGroupSelectFieldValue(name);
                return groups.stream().map(Group::getCode)
                        .reduce((accum, value) -> accum + this.delimiter + value)
                        .orElse("");
            case STATUS_ASSIGNEE:
                List<User> users2 = this.record.getStatusAssigneeFieldValue();
                return usersToString(users2);
            case SUBTABLE:
                List<TableRow> subTableValueItem = this.record.getSubtableFieldValue(name);
                return gson.toJson(subTableValueItem);
            case CREATOR:
                User creator = record.getCreatorFieldValue();
                return creator.getCode();
            case MODIFIER:
                User user = record.getModifierFieldValue();
                return user.getCode();
            case CHECK_BOX:
                List<String> list1 = this.record.getCheckBoxFieldValue(name);
                return ItemListToString(list1);
            case MULTI_SELECT:
                List<String> list2 = this.record.getMultiSelectFieldValue(name);
                return ItemListToString(list2);
            case CATEGORY:
                List<String> list3 = this.record.getCategoryFieldValue();
                return ItemListToString(list3);
            case FILE:
                List<FileBody> cbFileList = this.record.getFileFieldValue(name);
                return cbFileList.stream().map(FileBody::getFileKey)
                        .reduce((accum, value) -> accum + this.delimiter + value)
                        .orElse("");
            case NUMBER:
                return String.valueOf(this.record.getNumberFieldValue(name));
            case CALC:
                return this.record.getCalcFieldValue(name).toString();
            case CREATED_TIME:
                return this.record.getCreatedTimeFieldValue().toString();
            case DATE:
                return this.record.getDateFieldValue(name).toString();
            case DATETIME:
                return this.record.getDateTimeFieldValue(name).toString();
            case DROP_DOWN:
                return this.record.getDropDownFieldValue(name);
            case LINK:
                return this.record.getLinkFieldValue(name);
            case MULTI_LINE_TEXT:
                return this.record.getMultiLineTextFieldValue(name);
            case RADIO_BUTTON:
                return this.record.getRadioButtonFieldValue(name);
            case RECORD_NUMBER:
                return this.record.getRecordNumberFieldValue();
            case RICH_TEXT:
                return this.record.getRichTextFieldValue(name);
            case SINGLE_LINE_TEXT:
                return this.record.getSingleLineTextFieldValue(name);
            case STATUS:
                return this.record.getStatusFieldValue();
            case TIME:
                return this.record.getTimeFieldValue(name).toString();
            case UPDATED_TIME:
                return this.record.getUpdatedTimeFieldValue().toString();
            case SPACER:
            case GROUP:
            case HR:
            case LABEL:
            case REFERENCE_TABLE:
            default: // there is no type not listed above and all these types link to default action have no specific value
                return "";
        }
    }

    private String ItemListToString(List<String> list) {
        return list.stream()
                .reduce((accum, value) -> accum + this.delimiter + value)
                .orElse("");
    }

    private String usersToString(List<User> list) {
        return list.stream().map(User::getCode)
                .reduce((accum, value) -> accum + this.delimiter + value)
                .orElse("");
    }
}
