package jumpaku.curves.fsc.test.identify.nquarter

import jumpaku.commons.json.parseJson
import jumpaku.curves.fsc.identify.nquarter.NQuarterIdentifyResult
import jumpaku.curves.fsc.identify.nquarter.NQuarterIdentifyResultJson
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test

class NQuarterIdentifyResultJsonTest {

    @Test
    fun testNQuarterIdentifyResultJson() {
        println("NQuarterIdentifyResultJson")
        val e = str.parseJson().let { NQuarterIdentifyResultJson.fromJson(it) }
        val a = NQuarterIdentifyResultJson.toJsonStr(e).parseJson().let { NQuarterIdentifyResultJson.fromJson(it) }

        assertThat(a, `is`(closeTo(e)))
    }

    val str = """{
  "grades": [
    {
      "key": "Quarter1",
      "value": 0.0
    },
    {
      "key": "Quarter2",
      "value": 0.7686302101390838
    },
    {
      "key": "Quarter3",
      "value": 0.0
    },
    {
      "key": "General",
      "value": 0.23136978986091616
    }
  ],
  "nQuarter1": {
    "base": {
      "begin": {
        "x": 198.82130711525807,
        "y": 363.7935542997682,
        "z": -0.0,
        "r": 43.85127619627875
      },
      "far": {
        "x": 474.63975058118274,
        "y": 251.51176764119901,
        "z": -0.0,
        "r": 64.99674228336292
      },
      "end": {
        "x": 749.0682337098037,
        "y": 367.14972191178947,
        "z": -0.0,
        "r": 34.25788628740388
      },
      "weight": 0.7071067811865476
    },
    "domain": {
      "begin": 0.0,
      "end": 1.0
    }
  },
  "nQuarter2": {
    "base": {
      "begin": {
        "x": 198.82130711525807,
        "y": 363.7935542997682,
        "z": -0.0,
        "r": 43.85127619627875
      },
      "far": {
        "x": 475.6226009612707,
        "y": 90.34817326390915,
        "z": 0.0,
        "r": 64.99674228336292
      },
      "end": {
        "x": 749.0682337098037,
        "y": 367.14972191178947,
        "z": -0.0,
        "r": 34.25788628740388
      },
      "weight": 6.123233995736766E-17
    },
    "domain": {
      "begin": 0.0,
      "end": 1.0
    }
  },
  "nQuarter3": {
    "base": {
      "begin": {
        "x": 198.82130711525807,
        "y": 363.7935542997682,
        "z": -0.0,
        "r": 43.85127619627875
      },
      "far": {
        "x": 477.9954116786622,
        "y": -298.73516204254054,
        "z": 0.0,
        "r": 64.99674228336292
      },
      "end": {
        "x": 749.0682337098037,
        "y": 367.14972191178947,
        "z": -0.0,
        "r": 34.25788628740388
      },
      "weight": -0.7071067811865475
    },
    "domain": {
      "begin": 0.0,
      "end": 1.0
    }
  },
  "general": {
    "base": {
      "begin": {
        "x": 198.82130711525807,
        "y": 363.7935542997682,
        "z": -0.0,
        "r": 43.85127619627875
      },
      "far": {
        "x": 475.4950576497242,
        "y": 111.2621790414419,
        "z": -0.0,
        "r": 64.99674228336292
      },
      "end": {
        "x": 749.0682337098037,
        "y": 367.14972191178947,
        "z": -0.0,
        "r": 34.25788628740388
      },
      "weight": 0.0788970718610822
    },
    "domain": {
      "begin": 0.0,
      "end": 1.0
    }
  }
}"""
}
