import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.Closeable
import java.util.function.BiFunction

class EulerMethod(
    xStart: Double,
    yStart: Double,
    xEnd: Double,
    stepSize: Double,
    differentialEquation: BiFunction<Double, Double, Double>,
): Closeable, AutoCloseable {
    private var _differentialEquation = differentialEquation
    private var differentialEquation
        get() = _differentialEquation
        set(value) {
            _differentialEquation = value
            eulerFull()
        }

    private var _stepSize = stepSize
    private var stepSize
        get() = _stepSize
        set(value) {
            _stepSize = value
            eulerFull()
        }

    private var _xEnd = xEnd
    private var xEnd
        get() = _xEnd
        set(value) {
            _xEnd = value
            eulerFull()
        }

    private var _xStart = xStart
    private var xStart
        get() = _xStart
        set(value) {
            _xStart = value
            eulerFull()
        }

    private var _yStart = yStart
    private var yStart
        get() = _yStart
        set(value) {
            _yStart = value
            eulerFull()
        }

    private val _state: MutableStateFlow<List<Pair<Double, Double>>?> = MutableStateFlow(null)
    val state: StateFlow<List<Pair<Double, Double>>?>
        get() = _state

    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        eulerFull()
    }
    private fun eulerStep(
        xCurrent: Double,
        stepSize: Double,
        yCurrent: Double,
        differentialEquation: BiFunction<Double, Double, Double>
    ): Double {
        require(!(stepSize <= 0)) { "stepSize should be greater than zero" }
        return yCurrent + stepSize * differentialEquation.apply(xCurrent, yCurrent)
    }
    private fun eulerFull() {
        require(!(xStart >= xEnd)) { "xEnd should be greater than xStart" }
        require(!(stepSize <= 0)) { "stepSize should be greater than zero" }

        val points = ArrayList<Pair<Double, Double>>()
        points += xStart to yStart
        var yCurrent = yStart
        var xCurrent = xStart

        while (xCurrent < xEnd) {
            yCurrent = eulerStep(xCurrent, stepSize, yCurrent, differentialEquation)
            xCurrent += stepSize
            points += xCurrent to yCurrent
        }
        scope.launch(Dispatchers.IO) {
            _state.emit(points)
        }
    }

    override fun close() {
        scope.cancel()
    }
}