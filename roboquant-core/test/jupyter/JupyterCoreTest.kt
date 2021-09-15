package jupyter

import org.roboquant.jupyter.JupyterCore
import org.roboquant.jupyter.Output
import org.junit.Test

internal class JupyterCoreTest {

    @Test
    fun test() {
        JupyterCore()
        Output.notebook(true)
        Output.lab()
    }

}