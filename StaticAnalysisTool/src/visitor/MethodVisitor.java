package visitor;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public class MethodVisitor extends VoidVisitorAdapter<Void> {
    private String targetMethodName;
    private MethodDeclaration foundMethod = null;
    private int numOfPara;
    
    public MethodVisitor(String targetMethodName, int num) {
        this.targetMethodName = targetMethodName;
        this.numOfPara = num;
    }

    @Override
    public void visit(MethodDeclaration md, Void arg) {
        super.visit(md, arg);
        if (md.getNameAsString().equals(targetMethodName) && md.getParameters().size() == numOfPara) {
            foundMethod = md;
        }
    }

    public MethodDeclaration getFoundMethod() {
        return foundMethod;
    }
}

