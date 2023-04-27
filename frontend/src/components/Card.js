import React from "react";

export default function Card(props) {
	const { user } = props;

	return (
		<div className="Card" style={{ padding: "10px 20px" }}>
			<h2>{user.username}</h2>
			<p>{user.email}</p>
			<p>Lorem ipsum</p>
		</div>
	);
}
