import { StyleSheet } from "react-native";

export default StyleSheet.create({
    container: {
        flex: 1,
    },
    scrollView: {
        flex: 1,
    },
    bar: {
        flexDirection: "row",
        borderRightWidth: 1,
        borderLeftWidth: 1,
    },
    beat: {
        flexDirection: "column-reverse",
    },
    pitch: {
        width: 50,
        flex: 1,
        borderWidth: 1,
        borderColor: "black",
    },
    selectedPitch: {
        backgroundColor: "pink",
    },
    playingPitch: {
        backgroundColor: "#ff80ff",
    },
    playingSelectedPitch: {
        backgroundColor: "purple",
    },
    tabBar: {
        height: 50,
    }
})