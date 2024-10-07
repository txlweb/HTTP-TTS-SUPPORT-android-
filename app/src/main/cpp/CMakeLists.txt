# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html.
# For more examples on how to use CMake, see https://github.com/android/ndk-samples.

# Sets the minimum CMake version required for this project.
cmake_minimum_required(VERSION 3.22.1)

# Declares the project name. The project name can be accessed via ${ PROJECT_NAME},
# Since this is the top level CMakeLists.txt, the project name is also accessible
# with ${CMAKE_PROJECT_NAME} (both CMake variables are in-sync within the top level
# build script scope).
project("ttssupport")

set(CMAKE_CXX_FLAGS " -O3 -fopenmp -std=c++11 ")
set(CMAKE_C_FLAGS " -O3 -fopenmp -std=c++11 ")




# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.
#
# In this top level CMakeLists.txt, ${CMAKE_PROJECT_NAME} is used to define
# the target library name; in the sub-module's CMakeLists.txt, ${PROJECT_NAME}
# is preferred for the same purpose.
#
# In order to load a library into your app from Java/Kotlin, you must call
# System.loadLibrary() and pass the name of the library defined here;
# for GameActivity/NativeActivity derived applications, the same library name must be
# used in the AndroidManifest.xml file.
add_library(${CMAKE_PROJECT_NAME} SHARED
        # List C/C++ source files with relative paths to this CMakeLists.txt.
        native-lib.cpp
        ./src/tn/glog/src/demangle.cc
        ./src/tn/glog/src/logging.cc
        ./src/tn/glog/src/raw_logging.cc
        ./src/tn/glog/src/symbolize.cc
        ./src/tn/glog/src/utilities.cc
        ./src/tn/glog/src/vlog_is_on.cc
        ./src/tn/glog/src/signalhandler.cc
        ./src/tn/gflags/src/gflags.cc
        ./src/tn/gflags/src/gflags_reporting.cc
        ./src/tn/gflags/src/gflags_completions.cc
        ./src/tn/openfst/src/lib/compat.cc
        ./src/tn/openfst/src/lib/flags.cc
        ./src/tn/openfst/src/lib/fst.cc
        ./src/tn/openfst/src/lib/fst-types.cc
        ./src/tn/openfst/src/lib/mapped-file.cc
        ./src/tn/openfst/src/lib/properties.cc
        ./src/tn/openfst/src/lib/symbol-table.cc
        ./src/tn/openfst/src/lib/symbol-table-ops.cc
        ./src/tn/openfst/src/lib/util.cc
        ./src/tn/openfst/src/lib/weight.cc
        ./src/tn/processor.cc
        ./src/tn/token_parser.cc
        ./src/tn/utf8_string.cc
        ./src/engipa/EnglishText2Id.cpp
        ./src/engipa/InitIPASymbols.cpp
        ./src/engipa/alphabet.cpp
        ./src/engipa/ipa.cpp
        ./src/hz2py/hanzi2phoneid.cpp
        ./src/hz2py/Hanz2Piny.cpp
        ./src/hz2py/pinyinmap.cpp
        ./src/nn_op/nn_conv1d.cpp
        ./src/nn_op/nn_softmax.cpp
        ./src/nn_op/nn_layer_norm.cpp
        ./src/nn_op/nn_relu.cpp
        ./src/nn_op/nn_gelu.cpp
        ./src/nn_op/nn_tanh.cpp
        ./src/nn_op/nn_flip.cpp
        ./src/nn_op/nn_cumsum.cpp
        ./src/nn_op/nn_softplus.cpp
        ./src/nn_op/nn_clamp_min.cpp
        ./src/nn_op/nn_sigmoid.cpp
        ./src/nn_op/nn_conv1d_transposed.cpp
        ./src/nn_op/nn_leaky_relu.cpp
        ./src/platform/tts_file_io.cpp
        ./src/platform/tts_logger.cpp
        ./src/utils/utils.cpp
        ./src/modules/iStft.cpp
        ./src/modules/hann.cpp
        ./src/modules/attention_encoder.cpp
        ./src/modules/multi_head_attention.cpp
        ./src/modules/ffn.cpp
        ./src/modules/ConvFlow.cpp
        ./src/modules/DDSConv.cpp
        ./src/modules/ElementwiseAffine.cpp
        ./src/modules/random_gen.cpp
        ./src/modules/ResidualCouplingLayer.cpp
        ./src/modules/ResBlock1.cpp
        ./src/modules/WN.cpp
        ./src/modules/pqmf.cpp
        ./src/models/TextEncoder.cpp
        ./src/models/StochasticDurationPredictor.cpp
        ./src/models/FixDurationPredictor.cpp
        ./src/models/DurationPredictor_base.cpp
        ./src/models/ResidualCouplingBlock.cpp
        ./src/models/Generator_base.cpp
        ./src/models/Generator_hifigan.cpp
        ./src/models/Generator_MS.cpp
        ./src/models/Generator_Istft.cpp
        ./src/models/Generator_MBB.cpp
        ./src/models/SynthesizerTrn.cpp)
target_include_directories(${CMAKE_PROJECT_NAME} PUBLIC ./eigen-3.4.0
        ./src/tn/header
        ./include
        ./src/header)
# Specifies libraries CMake should link to your target library. You
# can link libraries from various origins, such as libraries defined in this
# build script, prebuilt third-party libraries, or Android system libraries.
target_link_libraries(${CMAKE_PROJECT_NAME}
        # List libraries link to the target library
        android
        log)