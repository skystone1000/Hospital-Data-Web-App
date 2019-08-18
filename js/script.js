//https://www.youtube.com/watch?v=Ds5tm-6Nc9g&list=LLvbU1uZf9uOtn25YutZrSaQ&index=2&t=0s
//using annyang
if (annyang) {
// Add our commands to annyang

var commands = {
    'hello': function() { alert('Hello world!'); },
    'what is this': function() { alert('what is this ?'); },
    
    'reload the page': function(){
        window.location.reload();
    },
    'stop listening': function() {
        annyang.abort();
    },
    'submit': function() {
        document.getElementById('submitBtn').click();
    },


    'first name *val': function(val){
        document.getElementById('firstName').value = val;
    },
    'middle name *val': function(val){
        document.getElementById('middleName').value = val;
    },
    'last name *val': function(val){
        document.getElementById('lastName').value = val;
    },
    'age *val': function(val){
        document.getElementById('age').value = val;
    },
    'gender *val': function(val){
        document.getElementById('sex').value = val;
    },
    'occupation *val': function(val){
        document.getElementById('occupation').value = val;
    },
    'address *val': function(val){
        document.getElementById('address').value = val;
    },
    'phone number *val': function(val){
        document.getElementById('phone').value = val;
    },
    'registration number *val': function(val){
        document.getElementById('regno').value = val;
    }

}



/*
annyang.addCallback('result',function(whatWasHeard) {
  document.getElementById("voiceToText").innerHTML = whatWasHeard[0];
});
*/





annyang.addCommands(commands);
annyang.start({ autoRestart: true, continuous: false });
annyang.debug();

// Tell KITT to use annyang
SpeechKITT.annyang();
// Define a stylesheet for KITT to use
SpeechKITT.setStylesheet('//cdnjs.cloudflare.com/ajax/libs/SpeechKITT/0.3.0/themes/flat.css');
// Render KITT's interface
SpeechKITT.vroom();
//SpeechKITT.setInstructionsText(string)

}
