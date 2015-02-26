package org.jboss.pnc.rest.restmodel;

import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.model.builder.ProductVersionBuilder;
import org.jboss.pnc.model.builder.BuildConfigurationBuilder;
import org.jboss.pnc.model.builder.BuildConfigurationSetBuilder;

import javax.xml.bind.annotation.XmlRootElement;

import java.util.List;
import java.util.stream.Collectors;

import static org.jboss.pnc.rest.utils.StreamHelper.nullableStreamOf;
import static org.jboss.pnc.rest.utils.Utility.performIfNotNull;

@XmlRootElement(name = "BuildConfigurationSet")
public class BuildConfigurationSetRest {

    private Integer id;

    private String name;

    private Integer productVersionId;

    private List<Integer> buildConfigurationIds;

    public BuildConfigurationSetRest() {
    }

    public BuildConfigurationSetRest(BuildConfigurationSet buildConfigurationSet) {
        this.id = buildConfigurationSet.getId();
        this.name = buildConfigurationSet.getName();
        performIfNotNull(buildConfigurationSet.getProductVersion() != null, () ->this.productVersionId = buildConfigurationSet.getProductVersion().getId());
        this.buildConfigurationIds = nullableStreamOf(buildConfigurationSet.getBuildConfigurations())
                .map(buildConfiguration -> buildConfiguration.getId())
                .collect(Collectors.toList());

    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getProductVersionId() {
        return productVersionId;
    }

    public void setProductVersionId(Integer productVersionId) {
        this.productVersionId = productVersionId;
    }

    public List<Integer> getBuildConfigurationIds() {
        return buildConfigurationIds;
    }

    public void setBuildConfigurationIds(List<Integer> buildConfigurationIds) {
        this.buildConfigurationIds = buildConfigurationIds;
    }

    public BuildConfigurationSet toBuildConfigurationSet() {
        BuildConfigurationSetBuilder builder = BuildConfigurationSetBuilder.newBuilder();
        builder.name(name);
        performIfNotNull(productVersionId != null, () -> builder.productVersion(ProductVersionBuilder.newBuilder().id(productVersionId).build()));

        return builder.build();
    }

}