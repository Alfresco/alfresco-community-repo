package org.alfresco.repo.node.db;

public sealed interface Java17Test permits OnlyThis, AndThat {

    void justDoIt();
    static Java17Test getInstance() {
        return Math.random() < 0.5 ? new OnlyThis() : new AndThat();
    }

}

final class OnlyThis implements Java17Test {
    @Override
    public void justDoIt()
    {
        System.out.println("Only this");
    }
}

final class AndThat implements Java17Test {
    @Override
    public void justDoIt()
    {
        System.out.println("And that");
    }
}

//final class NotThisOne implements Java17Test {
//
//}