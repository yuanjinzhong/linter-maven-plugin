import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * @author: yuanjinzhong
 * @date: 2023/5/6 18:54
 * @description:
 */
@Mojo(name = "sayhi",defaultPhase= LifecyclePhase.NONE)
public class GreetingMojo extends AbstractMojo {
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info( "Hello, world." );
    }
}
