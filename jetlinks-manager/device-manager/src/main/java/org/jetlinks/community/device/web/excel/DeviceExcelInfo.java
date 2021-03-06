package org.jetlinks.community.device.web.excel;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.reactor.excel.CellDataType;
import org.hswebframework.reactor.excel.ExcelHeader;
import org.hswebframework.web.bean.FastBeanCopier;
import org.jetlinks.community.device.entity.DeviceTagEntity;
import org.jetlinks.core.metadata.PropertyMetadata;
import org.springframework.util.StringUtils;

import javax.validation.constraints.NotBlank;
import java.util.*;

@Getter
@Setter
public class DeviceExcelInfo {

    @NotBlank(message = "设备ID不能为空")
    private String id;

    @NotBlank(message = "设备名称不能为空")
    private String name;

    private String orgId;

    private String productName;

    private String parentId;

    private List<DeviceTagEntity> tags = new ArrayList<>();

    public void tag(String key, String name, Object value) {
        if (value == null) {
            return;
        }
        DeviceTagEntity entity = new DeviceTagEntity();
        entity.setKey(key);
        entity.setValue(String.valueOf(value));
        entity.setName(name);
        entity.setId(String.valueOf(id).concat(":").concat(key));
        tags.add(entity);
    }

    public void setId(String id) {
        this.id = id;
        for (DeviceTagEntity tag : tags) {
            tag.setId(String.valueOf(id).concat(":").concat(tag.getKey()));
        }
    }

    public void with(String key, Object value) {
        FastBeanCopier.copy(Collections.singletonMap(key, value), this);
    }

    public Map<String,Object> toMap(){
        Map<String,Object> val = FastBeanCopier.copy(this,new HashMap<>());
        for (DeviceTagEntity tag : tags) {
            val.put(tag.getKey(),tag.getValue());
        }
        return val;
    }

    public static List<ExcelHeader> getTemplateHeaderMapping(List<PropertyMetadata> tags) {
        List<ExcelHeader> arr = new ArrayList<>(Arrays.asList(
            new ExcelHeader("id", "设备ID", CellDataType.STRING),
            new ExcelHeader("name", "设备名称", CellDataType.STRING),
            new ExcelHeader("orgId", "所属机构ID", CellDataType.STRING),
            new ExcelHeader("parentId", "父设备ID", CellDataType.STRING)
        ));
        for (PropertyMetadata tag : tags) {
            arr.add(new ExcelHeader(tag.getId(), StringUtils.isEmpty(tag.getName()) ? tag.getId() : tag.getName(), CellDataType.STRING));
        }
        return arr;
    }

    public static List<ExcelHeader> getExportHeaderMapping(List<PropertyMetadata> tags) {
        List<ExcelHeader> arr = new ArrayList<>(Arrays.asList(
            new ExcelHeader("id", "设备ID", CellDataType.STRING),
            new ExcelHeader("name", "设备名称", CellDataType.STRING),
            new ExcelHeader("productName", "设备型号", CellDataType.STRING),
            new ExcelHeader("orgId", "所属机构ID", CellDataType.STRING),
            new ExcelHeader("parentId", "父设备ID", CellDataType.STRING)
        ));
        for (PropertyMetadata tag : tags) {
            arr.add(new ExcelHeader(tag.getId(), StringUtils.isEmpty(tag.getName()) ? tag.getId() : tag.getName(), CellDataType.STRING));
        }
        return arr;
    }

    public static Map<String, String> getImportHeaderMapping() {
        Map<String, String> mapping = new HashMap<>();

        mapping.put("设备ID", "id");
        mapping.put("设备名称", "name");
        mapping.put("名称", "name");

        mapping.put("所属机构", "orgId");
        mapping.put("父设备ID", "parentId");

        return mapping;
    }
}
