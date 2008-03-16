<?php
	set_include_path("/opt/PEAR/".PATH_SEPARATOR.get_include_path());
	set_include_path(get_include_path().PATH_SEPARATOR."../src/");
	define("CIFORM_AUTODECRYPT",FALSE); // for demo purpose only
	require_once("ciform.php");
?><html>
	<head>
		<link rel="stylesheet" href="ciform.css" media="screen">
		<script type="text/javascript" src="../target/libciform.js"></script>
		<script type="text/javascript" src="../src/ciform.js"></script><!-- for debug only -->
		<script type="text/javascript" src="keys/key-rsa.pub.js"></script>
		<script type="text/javascript">

			/**
				This function is only for demo purpose.
				Its goal is to show the user how the ciphertext looks like.
			*/
			function previewCipher()
			{
				// the following encoder is approaching the one used by Ciform
				var encoder = new ciform.RSAEncoder(CIFORM['pubKey'],{'preamble':true,'salt':true});
				document.getElementById('ciphertext').innerHTML = encoder.encode('in');
			}

			/**
				this function is only for demo purpose.
				It enables/disables SHA-1 encoding of the password before encrypting to server.
			*/
			function checkSha1(checkbox)
			{
				document.forms[0].output.className = checkbox.checked ? "hex ciform-sha1" : "hex";
			}

		</script>
	</head>
	<body>

		<h1>Demo page for CiForm</h1>

		<h2>1. Fill in the following form</h2>

		<p>On submit, a hidden output field takes the value of the encrypted password,
		and the password field is emptied so it is not transmitted.</p>

		<form action="<?= $_SERVER['PHP_SELF'] ?>" method="POST" onsubmit="javascript:return new ciform.Ciform(this,CIFORM['pubKey']).encryptFields([{'in':'password','out':'output'}],alert);">
			<input type="hidden" class="hex" name="output" size="80" onchange="javascript:document.getElementById('ciphertext').innerHTML=this.value;">
			login : <input type="text" name="user"><br>
			password : <input type="password" class="txt" name="password" onkeyup="this.onchange()" onchange="javascript:previewCipher();">
				<!-- TODO icon onclick : show the public key and the server url -->
				<img id="logo" src="pix/green-lock.gif" style="vertical-align:middle;">
				Encrypt using SHA-1 : <input type="checkbox" name="usesha1" onchange="javascript:checkSha1(this);">
				<br>
				Preview of the encrypted text * : <span id="ciphertext" class="hex"></span>
				<br>
			<input type="submit" value="LOGIN">
		</form>

		<p>* <smaller><i>On-the-fly encryption may slow down typing, the usual case is to encrypt only on form submit.</i></smaller></p>

		<h2>2. The form is submitted with the encrypted password</h2>

		<p>Here is what the received response looks like :</p>
		<pre><?php print_r($_REQUEST); ?></pre>

		<p>And after decryption :</p>
		<pre><?php
			$_REQUEST = ciform_decryptParams($_REQUEST,$_SESSION[CIFORM_SESSION][CIFORM_SESSION_KEYPAIR]);
			print_r($_REQUEST);
		?></pre>
	</body>
</html>