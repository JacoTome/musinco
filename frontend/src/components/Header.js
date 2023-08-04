import React from "react";
// Importing the CSS file
import "./Header.css";
export default function Header() {
	return (
		<div id="navbar">
			<div>
				<h1>Musinco</h1>
			</div>
			<div>
				<ul>
					<li>
						<a href="#navbar">Profile</a>
					</li>
					<li>
						<a href="#navbar">About</a>
					</li>
					<li>
						<a href="#navbar">Contact</a>
					</li>
				</ul>
			</div>
		</div>
	);
}
