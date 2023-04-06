import React from 'react';



const SingleCard = ({ name, email, id }) => {
    return (
      <div>
        <h2>{name} + {id}</h2>
        <p>{email}</p>
      </div>
    )
}

export default function Card() {
  return (
    <div className="Card">
      <h1>Card</h1>
      <SingleCard name="Jacopo" email="Popi" id="dop"/>

    </div>
  );
}
