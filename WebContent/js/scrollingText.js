function ScrollingText() {
    var text = "WebQuizz | Web-Programmierung";
    var begin = 0;
    var end = text.length;
    lauftext();

    function lauftext() {
        document.getElementsByName("header")[0].value = "" +
        text.substring(begin,end) + " " + text.substring(0,begin);
        begin ++;
        if(begin >= end) {
            begin = 0;
        }
        //Speed
        //window.setTimeout(function(){lauftext()}, 250);
    }
}