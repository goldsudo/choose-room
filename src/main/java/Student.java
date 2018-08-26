import java.util.Arrays;

public class Student {
    private String name;
    private String targetBed;
    private String[] targetBeds;

    public Student(String name) {
        this.name = name;
    }

    public Student(String name, String targetBed) {
        this.name = name;
        this.targetBed = targetBed;
    }

    public Student(String name, String[] targetBeds) {
        this.name = name;
        this.targetBeds = targetBeds;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTargetBed() {
        return targetBed;
    }

    public void setTargetBed(String targetBed) {
        this.targetBed = targetBed;
    }

    public String[] getTargetBeds() {
        return targetBeds;
    }

    public void setTargetBeds(String[] targetBeds) {
        this.targetBeds = targetBeds;
    }

    @Override
    public String toString() {
        return "Student{" +
                "name='" + name + '\'' +
                ", targetBed='" + targetBed + '\'' +
                ", targetBeds=" + Arrays.toString(targetBeds) +
                '}';
    }
}
