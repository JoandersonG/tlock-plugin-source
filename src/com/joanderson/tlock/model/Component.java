package com.joanderson.tlock.model;

public abstract class Component {
    private String componentName;
    private String instanceName;
    private String originalName;
    private String id;

    public Component(String componentName, String originalName, String id) {
        this.componentName = componentName;
        this.originalName = originalName;
        this.id = id;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public abstract String toPiADL();

    @Override
    public boolean equals(Object obj) {
        return obj.getClass() == this.getClass() && ((Component) obj).getId().equals(id);
    }
}
