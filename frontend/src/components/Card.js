import React, { useEffect } from "react";

export default function Card(props) {
	const { user } = props;
	
	useEffect(() => {
		console.log(user);
	}, [user]);


	return (
		<div className="Card" style={{ padding: "10px 20px" }} >
			<h3>{user.musician.value}</h3>
		</div>
	);
}
